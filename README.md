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

The response schemas are now **closed exact contracts**: documented field names and types are explicit and `additionalProperties` is set to `false`. Unexpected fields, renamed fields, misplaced nested values, or wrong types therefore fail immediately instead of being accepted by a broad schema.

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

Requirement 6 removes alias searching. Business errors are now read only from the exact documented JSON paths `errorCode`, `errorMessage`, `errorCategory`, and `errorType`. A similarly named field at another path cannot satisfy the assertion.

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

---

## Additional Banking Test Scenarios

The framework now includes the additional company-requested banking scenarios in three focused test classes:

- `AdditionalTransferScenariosTest`
  - transfer entire available balance
  - same-account transfer
  - invalid source account
  - invalid destination account
  - missing amount / source / destination
  - excessive decimal precision
  - very large transfer amount
  - duplicate transfer submission
- `AdditionalCustomerScenariosTest`
  - duplicate customer
  - missing mandatory customer fields
  - invalid mobile number
  - invalid customer status
  - update non-existing customer
  - delete non-existing customer
- `AdditionalAccountScenariosTest`
  - duplicate account
  - negative opening balance
  - invalid account type
  - missing mandatory account fields

### Validation depth

Successful/boundary transfers verify balances and strengthened transaction history. Rejected transfer scenarios capture balances and complete transaction-ID sets before the request and prove that both remain unchanged afterward. Customer/account validation also checks that rejected create requests do not leave partial records when a safe lookup is possible.

### Contract-sensitive expectations

The new expected status codes are isolated in `config.properties`. These are precise expectations, not broad alternatives. Because several requested behaviors (for example duplicate-transfer policy, non-existing update/delete behavior, and some validation-error codes) are not defined in the original take-home brief, verify them against the live Swagger/business contract before the final company execution. If the live service disagrees with the documented requirement, retain the precise assertion and report the discrepancy instead of weakening the test.

### Duplicate transfer assumption

The requested duplicate-submission scenario is implemented with `contract.transfer.duplicatePolicy=REJECT`. The first request must succeed; the second identical submission is expected to be rejected and must not create a second balance/history side effect. If the real API uses an idempotency-key mechanism or intentionally allows identical sequential transfers, align this test to that documented policy.


---

## Requirement 6 - Improved JSON Validation Approach

The previous recursive field-search helper has been removed. The framework now uses three complementary validation layers:

1. **Explicit JSON Path validation** - known fields are read from one exact path, for example `customerNumber`, `city`, `balance`, `[0].transactionId`, and `errorCode`.
2. **Strict response DTOs / models** - customer, account, transfer, transaction, business-error, and ProblemDetail responses are deserialized into dedicated Java records by `StrictResponseMapper`. Unknown JSON properties are rejected.
3. **Exact JSON Schema contract validation** - response schemas use exact field names/types and `additionalProperties: false`; broad aliases and recursive discovery are not used.

### Main implementation files

- `validation/ExplicitJsonPathValidator.java`
- `validation/StrictResponseMapper.java`
- `dto/CustomerResponseDto.java`
- `dto/AccountResponseDto.java`
- `dto/TransferResponseDto.java`
- `dto/TransactionResponseDto.java`
- `dto/BusinessErrorResponseDto.java`
- `dto/ProblemDetailResponseDto.java`

`JsonSupport.java`, which previously searched recursively through arbitrary JSON objects, has been removed. Transaction parsing and error parsing were also converted from alias-based field scans to exact DTO/JSON-path validation.

Example validation flow:

```text
HTTP status
    -> exact JSON Schema
    -> exact JSON Path
    -> strict DTO mapping
    -> business / persisted-state assertion
```

This prevents false positives such as accepting `customer.city` when the contract requires root `city`, accepting `accountName` when the contract requires `account_name`, or matching an unrelated nested `status` field.
