# Requirement 8 - Add Response Models

## Company feedback

Create response models and validate API responses through deserialization, including:

- `CustomerResponse`
- `AccountResponse`
- `TransferResponse`
- `TransactionResponse`

## Implementation

The framework now contains strongly typed response records under:

```text
src/test/java/com/assessment/banking/response/
├── CustomerResponse.java
├── AccountResponse.java
├── TransferResponse.java
├── TransactionResponse.java
├── BusinessErrorResponse.java
└── ProblemDetailResponse.java
```

`ResponseDeserializer` performs strict Jackson deserialization. Unknown fields, incompatible numeric types, trailing JSON tokens and invalid primitive null values are not silently accepted.

## Example

```java
CustomerResponse customer =
        ExplicitJsonPathValidator.customer(
                response,
                "Customer GET response");

Assert.assertEquals(customer.customerNumber(), expectedCustomerNumber);
Assert.assertEquals(customer.city(), expectedCity);
Assert.assertEquals(customer.status(), "ACTIVE");
```

For transaction history:

```java
List<TransactionResponse> transactions =
        ExplicitJsonPathValidator.transactions(
                response,
                "Transaction history response");
```

Each transaction item is deserialized before TransactionSupport converts it into the business-level `TransactionRecord` used by transfer assertions.

## Validation order

1. Expected HTTP status
2. JSON Schema contract validation
3. Explicit JSON Path validation
4. Strict response deserialization
5. Typed-model assertions
6. Persisted-state / business-rule assertions

## Result

The framework no longer depends on untyped response access for the core domain responses. Contract changes now fail either at JSON Schema validation or strict response-model deserialization, reducing false positives and improving maintainability.
