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

Before final submission, compare contract-sensitive statuses and business rules in
`src/test/resources/config.properties` with the live Swagger/OpenAPI. Request field names
are no longer runtime-configured: the exact wire contract is declared in the strongly
typed request models using `@JsonProperty`.


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
2. **Strict response models** - customer, account, transfer, transaction, business-error, and ProblemDetail responses are deserialized into dedicated Java records by `ResponseDeserializer`. Unknown JSON properties are rejected.
3. **Exact JSON Schema contract validation** - response schemas use exact field names/types and `additionalProperties: false`; broad aliases and recursive discovery are not used.

### Main implementation files

- `validation/ExplicitJsonPathValidator.java`
- `validation/ResponseDeserializer.java`
- `response/CustomerResponse.java`
- `response/AccountResponse.java`
- `response/TransferResponse.java`
- `response/TransactionResponse.java`
- `response/BusinessErrorResponse.java`
- `response/ProblemDetailResponse.java`

`JsonSupport.java`, which previously searched recursively through arbitrary JSON objects, has been removed. Transaction parsing and error parsing were also converted from alias-based field scans to exact DTO/JSON-path validation.

Example validation flow:

```text
HTTP status
    -> exact JSON Schema
    -> exact JSON Path
    -> strict response-model deserialization
    -> business / persisted-state assertion
```

This prevents false positives such as accepting `customer.city` when the contract requires root `city`, accepting `accountName` when the contract requires `account_name`, or matching an unrelated nested `status` field.


---

## Requirement 7 - Strongly Typed Request DTO Models

Generic `Map<String, Object>` request construction has been removed from the Java test framework. Request payloads are now represented by immutable Java 17 records:

- `request/CustomerRequest.java`
- `request/AccountRequest.java`
- `request/TransferRequest.java`

Each request model declares the exact wire-level JSON contract using Jackson `@JsonProperty`. For example, `AccountRequest.accountType` intentionally serializes to the documented API field `accout_type`, while `accountName` serializes to `account_name`.

### Type-safe API client

`BankApiClient` now accepts only the correct request model for each operation:

```java
createCustomer(CustomerRequest request)
updateCustomer(long customerNumber, CustomerRequest request)
createAccount(long customerNumber, AccountRequest request)
transfer(long customerNumber, TransferRequest request)
```

This prevents accidental submission of an account payload to a customer endpoint and removes runtime-only failures caused by misspelled map keys or wrong Java value types.

### Type-safe test data

`TestDataFactory` now returns request models rather than generic maps:

```java
CustomerRequest customer = TestDataFactory.customerRequest(customerNumber, "Chennai");
AccountRequest account = TestDataFactory.accountRequest(accountNumber, balance, "AUTO_SOURCE");
TransferRequest transfer = TestDataFactory.transferRequest(source, destination, amount);
```

Negative tests still cover missing mandatory fields without reverting to generic maps. The immutable request records provide explicit `without(...)` or `withoutSourceAccount()` / `withoutDestinationAccount()` / `withoutTransferAmount()` helpers. `@JsonInclude(NON_NULL)` causes the selected field to be omitted from the serialized JSON request.

### Result

- no `Map<String, Object>` request payloads remain under `src/test/java`
- exact JSON property names are version-controlled in request DTO annotations
- request construction is readable and refactor-friendly
- Java types protect numeric/monetary fields at compile time
- the API client exposes endpoint-specific request types
- negative-field tests remain explicit and maintainable


---

## Requirement 8 - Strongly Typed Response Models + Deserialization

The response side of the framework now uses explicit, strongly typed models rather than validating raw JSON alone.

### Response models

- `response/CustomerResponse.java`
- `response/AccountResponse.java`
- `response/TransferResponse.java`
- `response/TransactionResponse.java`
- `response/BusinessErrorResponse.java`
- `response/ProblemDetailResponse.java`

The four company-requested primary models are `CustomerResponse`, `AccountResponse`, `TransferResponse`, and `TransactionResponse`.

### Strict deserialization

`validation/ResponseDeserializer.java` deserializes API response bodies with Jackson configured to reject contract drift:

- `FAIL_ON_UNKNOWN_PROPERTIES`
- `FAIL_ON_NULL_FOR_PRIMITIVES`
- `FAIL_ON_TRAILING_TOKENS`
- `ACCEPT_FLOAT_AS_INT` disabled

A response containing an unexpected property or incompatible type therefore fails during deserialization instead of silently being accepted.

### Validation flow

```text
HTTP status
    -> JSON Schema contract
    -> explicit JSON Path checks
    -> ResponseDeserializer
    -> strongly typed Response model
    -> business / persisted-state assertions
```

Example:

```java
CustomerResponse customer =
        ExplicitJsonPathValidator.customer(
                response,
                "Customer GET response");

Assert.assertEquals(customer.city(), "Chennai");
Assert.assertEquals(customer.status(), "ACTIVE");
```

Account, transfer and transaction-history validations follow the same model-based approach. Transaction history is deserialized into `List<TransactionResponse>` so every item is checked against the exact response model before business assertions are performed.

This improves readability, type safety, maintainability and ensures response validation is based on the documented contract rather than loose JSON-field matching.

## Requirement 9 - Cleanup validation enhancement

Synthetic customer cleanup is now asserted, not merely logged. `BaseApiTest` delegates cleanup to `CleanupSupport`, validates the exact configured delete status, attempts all tracked resources, aggregates failures, and raises a TestNG after-method failure when cleanup is incomplete. If the test already has a primary failure/skip, cleanup diagnostics are attached as suppressed evidence so the original failure is preserved while cleanup failure is still visible.

---

## Requirement 10 - Secure Banking Logging / Sensitive-Data Masking

Logging has been hardened so banking identifiers and customer PII are not written to TestNG, console, CI, or diagnostic output in clear text.

### Central masking utility

`util/SensitiveDataMasker.java` is the single masking policy for the framework. It masks:

- `customerNumber`
- `accountNumber`
- `fromAccountNumber`
- `toAccountNumber`
- `mobileNum` / `mobileNumber`
- `transactionId`
- `referenceId`
- customer names and address fields in payload/error text
- customer/account identifiers embedded in endpoint paths and common error messages

Identifiers retain only the final four characters for troubleshooting. Direct PII such as first name, last name and address is fully redacted.

Example:

```text
Before:
[REQUEST] TransferRequest[fromAccountNumber=12345678, toAccountNumber=87654321, transferAmount=250.00]

After:
[REQUEST] Transfer | fromAccountNumber=****5678 | toAccountNumber=****4321 | transferAmount=<redacted>
```

### Request logging

`BankApiClient` no longer logs complete DTO payloads. It emits only operation metadata required for troubleshooting, with identifiers masked. Transfer amounts are redacted from normal logs.

### Response logging

Response bodies pass through `SensitiveDataMasker.sanitize(...)` before they reach TestNG/console output. This prevents a response containing customer, account, mobile, transaction or reference identifiers from leaking those values in logs.

### Failure and cleanup diagnostics

The same masking policy is applied to:

- HTTP assertion response-body diagnostics
- JSON Schema failure diagnostics
- strict DTO-deserialization failure diagnostics
- structured error-response diagnostics
- TestNG failure/skip listener output
- cleanup success/failure output
- setup-blocker response details

This keeps diagnostics useful while avoiding exposure of banking identifiers and customer PII.

## Requirement 11 - Enhanced Reporting (Extent Reports)

The framework now integrates **Extent Reports** in addition to TestNG/Surefire output.

After execution:

```text
target/extent-report.html
target/extent-artifacts/*.log
```

The HTML report includes:

- test execution summary and pass/fail/skip status,
- sanitized request details,
- sanitized response details,
- HTTP/status/business/schema validation results,
- failure and cleanup diagnostics,
- per-failure sanitized text-log attachments.

Because this assessment is API-only, browser screenshots are not applicable. The report explicitly records that on failures while attaching the relevant API execution log instead.

Run:

```bash
mvn clean test
```

Then open `target/extent-report.html` in a browser.
