# Requirement 10 - Improve Logging

## Company feedback

Current logging exposes complete payload and response information. The framework must mask account numbers, customer numbers and mobile numbers, and avoid exposing sensitive banking data in logs.

## Implementation

### 1. Central masking policy

Added:

`src/test/java/com/assessment/banking/util/SensitiveDataMasker.java`

The helper masks customer/account/mobile identifiers and sanitizes request, response, exception, endpoint-path and cleanup text before it is logged.

### 2. Request payload logging removed

`BankApiClient` no longer logs complete request DTO `toString()` values.

Examples of safe output:

```text
[REQUEST] Create customer | customerNumber=****5628 | mobileNum=******4094 | status=ACTIVE
[REQUEST] Create account | customerNumber=****5628 | accountNumber=****9912 | accountType=CURRENT
[REQUEST] Transfer | customerNumber=****5628 | fromAccountNumber=****9912 | toAccountNumber=****1130 | transferAmount=<redacted>
```

Names, addresses and complete financial payloads are not logged.

### 3. Response body sanitization

API responses are sanitized before being written to TestNG/console output. Sensitive JSON fields are masked even when the response contains full customer/account data.

### 4. Error/failure diagnostics sanitized

Masking is also applied to assertion failures, JSON Schema diagnostics, DTO deserialization diagnostics, TestNG listener messages and setup blocker messages. This prevents sensitive values from reappearing indirectly through failure logs.

### 5. Cleanup logs sanitized

Cleanup success/failure messages now mask the tracked customer number and sanitize underlying exception details.

## Result

The framework preserves enough information for debugging (last four characters of identifiers, HTTP status, endpoint/action and validation failure reason) without exposing complete banking identifiers or customer PII.
