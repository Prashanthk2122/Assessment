package com.assessment.banking.assertion;

import java.math.BigDecimal;

import org.testng.Assert;

import io.restassured.response.Response;

public final class ApiAssertions {

    private ApiAssertions() {
    }

    public static void status(
            Response response,
            int expectedStatus,
            String operation) {

        Assert.assertNotNull(
                response,
                operation + " returned a null response.");

        Assert.assertEquals(
                response.statusCode(),
                expectedStatus,
                operation
                        + " returned an unexpected HTTP status."
                        + System.lineSeparator()
                        + "Status line: "
                        + response.statusLine()
                        + System.lineSeparator()
                        + "Response body: "
                        + safeBody(response)
                        + System.lineSeparator());
    }

    public static void emptyBody(
            Response response,
            String operation) {

        String body = safeBody(response).trim();

        Assert.assertTrue(
                body.isEmpty(),
                operation
                        + " should return an empty body for an absent resource."
                        + System.lineSeparator()
                        + "Actual body: "
                        + body);
    }

    public static void absent(
            Response response,
            int expectedStatus,
            String operation) {

        status(response, expectedStatus, operation);
        emptyBody(response, operation);
    }

    public static void meaningfulError(
            Response response,
            String operation) {

        String body = safeBody(response).trim();

        Assert.assertTrue(
                !body.isEmpty()
                        && !body.equals("{}")
                        && !body.equalsIgnoreCase("null"),
                operation
                        + " should return a meaningful error body."
                        + System.lineSeparator()
                        + "Actual body: "
                        + body);
    }

    public static void moneyEquals(
            BigDecimal expected,
            BigDecimal actual,
            String message) {

        Assert.assertNotNull(
                actual,
                message + " Actual value was null.");

        Assert.assertTrue(
                expected.compareTo(actual) == 0,
                message
                        + System.lineSeparator()
                        + "Expected: "
                        + expected.toPlainString()
                        + System.lineSeparator()
                        + "Actual: "
                        + actual.toPlainString());
    }

    private static String safeBody(
            Response response) {

        if (response == null || response.getBody() == null) {
            return "";
        }

        String body = response.asString();

        return body == null ? "" : body;
    }
}
