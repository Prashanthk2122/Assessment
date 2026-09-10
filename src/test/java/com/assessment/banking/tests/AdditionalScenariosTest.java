package com.assessment.banking.tests;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.assessment.banking.assertion.ApiAssertions;
import com.assessment.banking.base.BaseApiTest;
import com.assessment.banking.config.TestConfig;
import com.assessment.banking.util.TestDataFactory;

import io.restassured.response.Response;

public class AdditionalScenariosTest
        extends BaseApiTest {

    @Test(description =
            "Additional scenario: account creation for an unknown customer "
            + "must be rejected with a meaningful error.")
    public void accountCreationForUnknownCustomerIsRejected() {

        long unknownCustomer =
                TestDataFactory.likelyUnknownCustomerNumber();

        long accountNumber =
                TestDataFactory.nextAccountNumber();

        Response createResponse =
                api.createAccount(
                        unknownCustomer,
                        TestDataFactory.accountPayload(
                                accountNumber,
                                new BigDecimal("100.00"),
                                "AUTO_UNKNOWN_CUSTOMER"));

        ApiAssertions.status(
                createResponse,
                TestConfig.getInt(
                        "status.unknownCustomer"),
                "Create account for unknown customer");

        ApiAssertions.meaningfulError(
                createResponse,
                "Unknown-customer account creation");

        Assert.assertTrue(
                createResponse.asString()
                        .toLowerCase()
                        .contains("customer"),
                "The error message should identify the customer problem.");

        Response accountLookup =
                api.getAccount(accountNumber);

        ApiAssertions.status(
                accountLookup,
                TestConfig.getInt(
                        "status.account.notFound"),
                "Verify rejected account was not created");

        ApiAssertions.meaningfulError(
                accountLookup,
                "Rejected account lookup");
    }   

    @Test(description =
            "Additional scenario: malformed customer JSON must be rejected "
            + "and must not persist a partial customer.")
    public void malformedCustomerJsonIsRejectedWithoutPartialCreation() {

        long customerNumber =
                TestDataFactory.nextCustomerNumber();

        String numberField =
                TestConfig.get(
                        "contract.customer.numberField",
                        "customerNumber");

        String firstNameField =
                TestConfig.get(
                        "contract.customer.firstNameField",
                        "firstName");

        String lastNameField =
                TestConfig.get(
                        "contract.customer.lastNameField",
                        "lastName");

        String malformedJson =
                "{"
                        + "\""
                        + numberField
                        + "\":"
                        + customerNumber
                        + ","
                        + "\""
                        + firstNameField
                        + "\":\"AUTO\","
                        + "\""
                        + lastNameField
                        + "\":"
                        + "}";

        Response response =
                api.createCustomerWithRawBody(
                        malformedJson);

        ApiAssertions.status(
                response,
                TestConfig.getInt(
                        "status.malformedJson"),
                "Submit malformed customer JSON");

        ApiAssertions.meaningfulError(
                response,
                "Malformed JSON request");

        Response lookup =
                api.getCustomer(customerNumber);

        ApiAssertions.absent(
                lookup,
                TestConfig.getInt(
                        "status.customer.absent"),
                "Verify malformed request created no customer");
    }
}
