# Requirement 13 - Additional Transaction Assertions

Implemented as executable framework assertions, not as documentation-only coverage.

## Added validations

- Unique transaction IDs across each returned transaction history.
- Configurable transaction ordering by timestamp (`ASC` / `DESC`).
- Timestamp format validation for ISO-8601 and positive epoch values.
- Plausibility check for absolute timestamps using a configurable future-clock-skew tolerance.
- Amount precision validation using `contract.transaction.maxDecimalPlaces=2`.
- Currency is mandatory in the exact transaction contract and must be a three-letter uppercase code.
- Debit and credit entries for one transfer must use the same currency.
- Optional exact currency assertion can be enabled with `contract.transaction.expectedCurrency` when Swagger documents a concrete code.
- Successful transfer debit and credit entries must match the configured transaction success status.

## Contract changes

`TransactionResponse` and `TransactionRecord` now include `currency`.
The transaction JSON Schema requires `currency` and constrains `transactionAmount` to increments of 0.01.
Explicit JSON Path validation also validates `transactionStatus`, `transactionTimestamp`, `transactionType`, and `currency` against the deserialized response model.

## Configuration

```properties
contract.transaction.order=DESC
contract.transaction.maxDecimalPlaces=2
contract.transaction.timestampFutureToleranceSeconds=300
contract.transaction.expectedCurrency=
```

`contract.transaction.order=DESC` is a precise configurable assumption because the assignment asks for ordering validation but does not state the direction. Confirm it against Swagger/OpenAPI before final execution if the API documents a different order.

The assignment states transfers use the same currency but does not name the currency, so the framework validates non-blank ISO-style currency plus debit/credit equality without inventing a currency code. If Swagger specifies one, set `contract.transaction.expectedCurrency`.
