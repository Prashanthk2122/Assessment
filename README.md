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


## Requirement 2 - API Contract Validation

The framework validates API response contracts using Rest-Assured JSON Schema Validator.

### What was added

- `io.rest-assured:json-schema-validator` test dependency.
- Central `ApiContractValidator` helper.
- Version-controlled schemas under `src/test/resources/schemas/`:
  - `customer-response-schema.json`
  - `account-response-schema.json`
  - `transfer-response-schema.json`
  - `transaction-response-schema.json`
  - `error-response-schema.json`
- Contract validation integrated into the existing customer, account, transfer, transaction-history and JSON-error flows.
- JSON Content-Type is checked before schema matching so a non-JSON contract change is reported clearly.

### Validation order

1. Precise HTTP status assertion.
2. Content-Type / JSON Schema contract assertion.
3. Business and persisted-state assertion.

This means a correct status code is not enough: missing required properties, changed field types, incompatible response structure or a non-JSON response will fail the same test with the schema path and actual response body in the diagnostic.

### Contract source and maintenance

The supplied Swagger/OpenAPI document is the source of truth. The schemas in this project reflect the contract currently known from the assignment/project mappings. Before final company submission, compare the five JSON files with the live OpenAPI response models. If the API contract is intentionally changed, update the schema and the corresponding test expectation together; do not loosen the schema merely to make a failing API pass.

### Current environment note

`POST /customers/add` has been observed returning HTTP 500 for unique synthetic setup requests. As a result, customer/account/transfer/transaction success-contract validation may be blocked until setup succeeds. Independent JSON error-contract scenarios still execute where their endpoints are reachable.
