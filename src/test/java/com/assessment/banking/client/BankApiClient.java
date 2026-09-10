package com.assessment.banking.client;

import java.util.Map;

import org.testng.Reporter;

import com.assessment.banking.config.TestConfig;
import com.assessment.banking.constants.Endpoints;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BankApiClient {

    private final RequestSpecification specification;

    public BankApiClient() {

        this.specification = new RequestSpecBuilder()
                .setBaseUri(TestConfig.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    private RequestSpecification request() {
        return RestAssured
                .given()
                .spec(specification);
    }

    private Response log(
            String operation,
            Response response) {

        Reporter.log(
                "[API] "
                        + operation
                        + " -> HTTP "
                        + response.statusCode()
                        + " | Body: "
                        + safeBody(response),
                true);

        return response;
    }

    private String safeBody(Response response) {

        if (response == null || response.getBody() == null) {
            return "<no body>";
        }

        String body = response.asString();

        return body == null || body.isBlank()
                ? "<empty>"
                : body;
    }

    public Response createCustomer(
            Map<String, Object> payload) {

        Reporter.log(
                "[REQUEST] Create customer payload: "
                        + payload,
                true);

        Response response = request()
                .body(payload)
                .when()
                .post(Endpoints.ADD_CUSTOMER);

        return log("POST " + Endpoints.ADD_CUSTOMER, response);
    }

    public Response createCustomerWithRawBody(
            String rawJson) {

        Reporter.log(
                "[REQUEST] Malformed customer payload: "
                        + rawJson,
                true);

        Response response = request()
                .body(rawJson)
                .when()
                .post(Endpoints.ADD_CUSTOMER);

        return log(
                "POST " + Endpoints.ADD_CUSTOMER
                        + " [malformed]",
                response);
    }

    public Response getCustomer(
            long customerNumber) {

        Response response = request()
                .pathParam(
                        "customerNumber",
                        customerNumber)
                .when()
                .get(Endpoints.CUSTOMER_BY_NUMBER);

        return log(
                "GET /customers/" + customerNumber,
                response);
    }

    public Response updateCustomer(
            long customerNumber,
            Map<String, Object> payload) {

        Response response = request()
                .pathParam(
                        "customerNumber",
                        customerNumber)
                .body(payload)
                .when()
                .put(Endpoints.CUSTOMER_BY_NUMBER);

        return log(
                "PUT /customers/" + customerNumber,
                response);
    }

    public Response deleteCustomer(
            long customerNumber) {

        Response response = request()
                .pathParam(
                        "customerNumber",
                        customerNumber)
                .when()
                .delete(Endpoints.CUSTOMER_BY_NUMBER);

        return log(
                "DELETE /customers/" + customerNumber,
                response);
    }

    public Response createAccount(
            long customerNumber,
            Map<String, Object> payload) {

        Reporter.log(
                "[REQUEST] Create account payload: "
                        + payload,
                true);

        Response response = request()
                .pathParam(
                        "customerNumber",
                        customerNumber)
                .body(payload)
                .when()
                .post(Endpoints.ADD_ACCOUNT);

        return log(
                "POST /accounts/add/" + customerNumber,
                response);
    }

    public Response getAccount(
            long accountNumber) {

        Response response = request()
                .pathParam(
                        "accountNumber",
                        accountNumber)
                .when()
                .get(Endpoints.ACCOUNT_BY_NUMBER);

        return log(
                "GET /accounts/" + accountNumber,
                response);
    }

    public Response transfer(
            long customerNumber,
            Map<String, Object> payload) {

        Reporter.log(
                "[REQUEST] Transfer payload: "
                        + payload,
                true);

        Response response = request()
                .pathParam(
                        "customerNumber",
                        customerNumber)
                .body(payload)
                .when()
                .put(Endpoints.TRANSFER);

        return log(
                "PUT /accounts/transfer/"
                        + customerNumber,
                response);
    }

    public Response getTransactions(
            long accountNumber) {

        Response response = request()
                .pathParam(
                        "accountNumber",
                        accountNumber)
                .when()
                .get(Endpoints.TRANSACTIONS);

        return log(
                "GET /accounts/transactions/"
                        + accountNumber,
                response);
    }
}
