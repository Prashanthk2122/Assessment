# Requirement 12 - Standardize Configuration Usage

## Review feedback
Some HTTP status assertions were configuration-driven while transaction-history checks and setup blocker detection still contained Java numeric literals.

## Implementation
All expected HTTP status values are now sourced from `src/test/resources/config.properties` through one typed access class:

`src/test/java/com/assessment/banking/config/ExpectedStatus.java`

No test, helper, cleanup component, or setup method should hardcode an expected HTTP status.

### Added configuration

```properties
status.transaction.get=200
status.serverError.minimum=500
```

`status.transaction.get` replaces the previously hardcoded `200` used for transaction-history GET validations.

`status.serverError.minimum` centralizes the setup-blocker threshold that previously used a Java `>= 500` literal. This is not a success expectation, but centralizing it removes the remaining HTTP status literal from framework flow control.

## Central access pattern

Instead of:

```java
ApiAssertions.status(response, 200, "Read transaction history");
```

or scattered property-key strings:

```java
ApiAssertions.status(
        response,
        TestConfig.getInt("status.account.get"),
        "Read account");
```

use:

```java
ApiAssertions.status(
        response,
        ExpectedStatus.transactionGet(),
        "Read transaction history");

ApiAssertions.status(
        response,
        ExpectedStatus.accountGet(),
        "Read account");
```

`ExpectedStatus` validates that every configured HTTP status is within the valid `100-599` range before returning it.

## Configuration override support

`TestConfig` continues to support JVM system-property overrides. For example:

```bash
mvn clean test -Dstatus.transfer.success=200 -Dstatus.transaction.get=200
```

This keeps environment/contract changes out of Java source code while preserving exact assertions.

## Result

- Customer expected statuses: configuration-driven
- Account expected statuses: configuration-driven
- Transfer expected statuses: configuration-driven
- Transaction-history expected status: configuration-driven
- Negative/boundary scenario statuses: configuration-driven
- Cleanup expected status: configuration-driven
- Setup server-error threshold: configuration-driven
- No hardcoded numeric HTTP status is used as an expected value in test assertions
