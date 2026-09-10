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
