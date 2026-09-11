# Requirement 7 - Replace Generic Maps with DTO Models

## Company feedback
The framework relied heavily on `Map<String, Object>` for request payloads. The requested improvement is to introduce strongly typed request models for customer, account and transfer operations.

## Implemented

### 1. Strongly typed request models
- `CustomerRequest`
- `AccountRequest`
- `TransferRequest`

All three are immutable Java 17 records and use Jackson `@JsonProperty` to declare the exact JSON request field names.

### 2. Generic request maps removed
`Map<String, Object>` and `LinkedHashMap` are no longer used for API request payloads under `src/test/java`.

### 3. Type-safe BankApiClient signatures
The API client now requires the correct request type per endpoint. This catches invalid request-object usage during compilation instead of at runtime.

### 4. TestDataFactory refactored
The factory now returns `CustomerRequest`, `AccountRequest`, and `TransferRequest` instances.

### 5. Negative tests remain strongly typed
Missing-field scenarios do not fall back to maps. Request records provide typed omission helpers and are annotated with `@JsonInclude(JsonInclude.Include.NON_NULL)` so a null test field is excluded from serialized JSON.

Examples:

```java
CustomerRequest request = TestDataFactory.customerRequest(customerNumber, "Chennai")
        .withStatus("NOT_A_VALID_STATUS");

AccountRequest request = TestDataFactory.accountRequest(accountNumber, balance, "AUTO")
        .without(AccountRequest.MandatoryField.BALANCE);

TransferRequest request = TestDataFactory.transferRequest(source, destination, amount)
        .withoutTransferAmount();
```

## Benefits
- improved readability
- compile-time type safety
- exact request field contract
- reduced typo/key mismatch risk
- easier refactoring and IDE navigation
- clearer enterprise-level API design

## Verification performed
- searched `src/test/java` for `Map<String, Object>`, `LinkedHashMap`, and `java.util.Map`: none remain
- compiled the new request records and `TestDataFactory` with Java 17 syntax/type checking
- preserved the previous response DTO, exact JSON Path, JSON Schema, error-validation, transaction-validation and additional banking scenario work

## Final execution
Run from the project root:

```bash
mvn clean test
```

The shared API may still block setup-dependent tests if `POST /customers/add` returns HTTP 500. Do not weaken expected assertions to hide that service defect.
