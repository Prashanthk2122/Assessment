# Requirement 11 - Enhance Reporting

## Implementation

**Extent Reports** is integrated as the professional HTML reporting layer.

### Added

- `com.aventstack:extentreports`
- `ExtentReportManager`
- `ReportLogger`
- enhanced `TestExecutionListener`

### Report content

1. **Request details** - operation and masked identifiers only.
2. **Response details** - HTTP status and sanitized response body.
3. **Validation results** - HTTP status, monetary assertions, JSON Schema and exact JSON Path/model validations.
4. **Execution summary** - provided by the Extent Spark HTML dashboard.
5. **Failure attachments** - sanitized `.log` files under `target/extent-artifacts/`.
6. **Cleanup/configuration failures** - recorded as explicit report failures.

### Banking-data safety

All values pass through `SensitiveDataMasker` before being written to the Extent report or attachment files. Account numbers, customer numbers, mobile numbers and other sensitive identifiers are never intentionally emitted in clear text.

### Screenshots

The project is API-only, so browser screenshots are not applicable. On test failures the report records this and attaches the sanitized API execution log instead.

### Output

```text
target/extent-report.html
target/extent-artifacts/*.log
```

### Run

```bash
mvn clean test
```
