# Requirement 9 - Improve Cleanup Validation

## What changed

The cleanup mechanism now validates actual cleanup success instead of logging the DELETE response only.

### Implemented

- Every tracked synthetic customer DELETE response is validated against `status.customer.delete`.
- A successful cleanup logs a structured `[CLEANUP PASS]` line with expected and actual status.
- Cleanup failures are not swallowed.
- All tracked cleanup operations are attempted even when one cleanup fails.
- Failures are aggregated and reported after all cleanup attempts.
- Cleanup diagnostics include the resource identifier, exception type, assertion/exception detail, HTTP status line and body when the failure originates from status validation.
- If the test already failed or was skipped, cleanup failure diagnostics are attached to the original throwable as a suppressed exception.
- Cleanup failure is also surfaced from `@AfterMethod`, ensuring Maven/TestNG cannot report the run as clean when synthetic-data deletion failed.

## Main code

- `com.assessment.banking.cleanup.CleanupSupport`
- `com.assessment.banking.cleanup.CleanupValidationException`
- Updated `com.assessment.banking.base.BaseApiTest`

## Design decision

Cleanup validation uses the same exact configured DELETE status as normal customer deletion. It does not accept a range of statuses and does not downgrade a cleanup problem to a warning.

## Run

```bash
mvn clean test
```
