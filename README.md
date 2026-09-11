# Banking API Automation Assessment

This Maven project is prepared for Eclipse 2024-06 and JDK 17.

## Required stack

- Java 17
- Maven
- Rest-Assured
- TestNG
- Jackson

## Run

```bash
mvn clean test
```

Single test:

```bash
mvn clean test -Dtest=CustomerLifecycleTest
```

## Covered assignment scenarios

- Customer lifecycle
- Successful transfer
- Non-positive transfer rejection
- Insufficient-funds rejection
- Unknown customer reference
- Malformed JSON
- Balance verification
- Debit / credit history verification
- Cleanup after tests
- Failure / blocker diagnostics

## Important

The project itself is configured to compile and execute through Maven.

The target banking service is external. If the deployed service returns a
real API defect, such as HTTP 500 for a valid setup request, the test must not
silently convert that into a pass. The assignment specifically expects
discrepancies and blockers to be reported.

Before final submission, compare the three contract-sensitive values in
`src/test/resources/config.properties` with the live Swagger:
- customer-create success status
- account-create success status
- transfer request field names


## API Contract Validation

JSON Schema validation is implemented for the response contracts requested in the assessment:

- `schemas/customer-response-schema.json`
- `schemas/account-response-schema.json`
- `schemas/transfer-response-schema.json`
- `schemas/transaction-response-schema.json`
- `schemas/error-response-schema.json`

`ApiContractValidator` centralizes Rest-Assured schema checks and adds the operation name, schema path, and actual response body to contract failures. Contract checks are executed alongside business assertions so status, schema, and persisted-state failures remain distinguishable.

The schemas are strict on the core fields/types used by the assessment and allow additional server fields so non-breaking additions do not create noise. If the live OpenAPI document defines a closed object model, set `additionalProperties` to `false` and align the exact documented field list before final submission.

---

## Structured Error Response Validation Enhancement

The framework now performs field-level error validation in addition to the JSON Schema validation introduced for API contracts.

### What is validated for business errors

`ApiErrorAssertions.businessError(...)` verifies:

1. **Complete JSON error structure** using `business-error-response-schema.json`.
2. **Error Code** using an exact expected code when documented (for example `CUSTOMER_NOT_FOUND` or `INSUFFICIENT_FUNDS`).
3. **Error Message** using scenario-specific semantic text.
4. **Error Category / Type** by requiring a non-empty category and/or type field.
5. The raw body is included in failures for diagnostics.

Supported field aliases allow the framework to work with common API naming conventions while still requiring the information to exist:

- Code: `errorCode` or `code`
- Message: `errorMessage`, `message`, or `detail`
- Category: `errorCategory` or `category`
- Type: `errorType` or `type`

### Error-code constants

`ErrorCodes.java` includes the assessment examples:

- `CUSTOMER_NOT_FOUND`
- `INVALID_ACCOUNT`
- `INSUFFICIENT_FUNDS`

An undocumented code is **not invented** merely to make a test green. For the non-positive transfer scenario, the framework validates that a structured non-empty code is returned and validates the message/category/type, because the assessment acceptance rules do not specify the exact code for that case.

### Parser / malformed JSON errors

Malformed JSON is a protocol/parser error rather than a business-domain error. The deployed service currently returns a Spring/RFC-7807 `ProblemDetail` shape. The framework validates the complete required structure separately using `problem-detail-error-schema.json`:

- `type`
- `title`
- `status`
- `detail`
- `instance`

This avoids fabricating a business code for an error type where the API does not document one.

### Current environment gap

The deployed unknown-customer response observed during assessment execution is currently similar to:

```json
{"message":"Customer with id: ... does not exist!"}
```

It does not expose the requested structured **error code** or **category/type**. With the enhanced assertions this is intentionally reported as an error-contract failure instead of being accepted merely because a message is present. This directly demonstrates the gap identified in the review feedback.

Run the final suite with:

```bash
mvn clean test
```


---

## Strengthened Transaction Validation

Transaction verification now goes beyond transaction type and amount.

For a successful transfer, each newly-created history entry must contain and validate:

- Transaction ID
- Reference / Correlation ID
- Transaction Status
- Account Number
- Transaction Timestamp
- Transaction Type
- Transaction Amount

The framework captures transaction IDs before the transfer, reads both histories after the transfer, and identifies exactly one new `DEBIT` on the source account and exactly one new `CREDIT` on the destination account.

`TransactionAssertions.successfulTransferPair(...)` then verifies:

1. Debit Transaction ID and Credit Transaction ID are present and distinct.
2. Debit reference/correlation ID and credit reference/correlation ID are present and equal.
3. Debit entry belongs to the source account.
4. Credit entry belongs to the destination account.
5. Both entries have the exact transfer amount.
6. Debit type is `DEBIT`; credit type is `CREDIT`.
7. Both statuses match `contract.transaction.successStatus` from `config.properties`.
8. Both timestamps are non-empty and parseable as ISO-8601 or a positive epoch value.

The equality of the reference/correlation IDs is the proof that the debit and credit entries belong to the **same transfer request**.

Rejected-transfer validation is also stronger: source and destination transaction-ID sets are captured before the rejected request and must remain unchanged afterward. This detects any hidden transaction creation even if type/amount matching alone would miss it.

### Important contract note

The live customer-creation endpoint is currently returning HTTP 500, so transfer setup is blocked in the shared environment. The transaction field names and exact success-status value must be confirmed against live Swagger/OpenAPI before the final company run. The framework intentionally fails if required transaction identity/correlation fields are missing rather than silently falling back to type-and-amount-only validation.
