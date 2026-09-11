# Requirement 2 - API Contract Validation

Implemented as a framework/code change, not as a document-only response.

## Files added

- `src/test/java/com/assessment/banking/assertion/ApiContractValidator.java`
- `src/test/resources/schemas/customer-response-schema.json`
- `src/test/resources/schemas/account-response-schema.json`
- `src/test/resources/schemas/transfer-response-schema.json`
- `src/test/resources/schemas/transaction-response-schema.json`
- `src/test/resources/schemas/error-response-schema.json`

## Files updated

- `pom.xml`
- `CustomerLifecycleTest.java`
- `SuccessfulTransferTest.java`
- `RejectedTransferTest.java`
- `AdditionalScenariosTest.java`
- `README.md`

## Review checklist

- Customer response schema validation: implemented.
- Account response schema validation: implemented.
- Transfer response schema validation: implemented.
- Transaction response schema validation: implemented.
- Error response schema validation: implemented.
- Validation failure includes schema path and raw response body.
- Final run command: `mvn clean test`.
