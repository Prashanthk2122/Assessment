# Requirement 6 - Improve JSON Validation Approach

## Company feedback

Replace recursive field searching with:

- Explicit JSON Path validations
- Response DTOs / Models
- Exact contract validation

## Implementation

### 1. Recursive search removed

`JsonSupport.java` has been deleted. No production test helper recursively scans the JSON tree or searches arbitrary nested objects for a matching field name.

### 2. Explicit JSON Path validation

`ExplicitJsonPathValidator` validates one documented path per field. Examples:

```java
response.jsonPath().getLong("customerNumber");
response.jsonPath().getString("city");
response.jsonPath().get("balance");
response.jsonPath().getString("[0].transactionId");
response.jsonPath().getString("errorCode");
```

The validator does not search aliases or nested objects when a field is missing.

### 3. Response DTOs / Models

Strict DTOs were added for:

- `CustomerResponseDto`
- `AccountResponseDto`
- `TransferResponseDto`
- `TransactionResponseDto`
- `BusinessErrorResponseDto`
- `ProblemDetailResponseDto`

`StrictResponseMapper` uses Jackson with `FAIL_ON_UNKNOWN_PROPERTIES`, `FAIL_ON_TRAILING_TOKENS`, and strict numeric handling. Unexpected contract fields are not silently ignored.

### 4. Exact JSON Schema contract validation

Customer, account, transfer, transaction, business-error, ProblemDetail, and generic error schemas were tightened:

- exact field names
- explicit field types
- required fields
- `additionalProperties: false`
- no broad alias alternatives

### 5. Existing tests updated

Customer lifecycle, account balance checks, successful/rejected transfers, transaction history parsing, and structured error validation now use the exact DTO/JSON-path approach.

## Result

A response can no longer pass because a similarly named field exists somewhere else in the JSON tree. A path rename, type change, unexpected property, or response-shape drift is reported as a contract failure.

## Run

```bash
mvn clean test
```

The shared service has previously returned HTTP 500 for customer setup. Such service defects/blockers remain visible; assertions are not weakened to force a green build.
