package com.assessment.banking.base;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.assessment.banking.assertion.ApiAssertions;
import com.assessment.banking.client.BankApiClient;
import com.assessment.banking.config.TestConfig;
import com.assessment.banking.model.AccountPair;
import com.assessment.banking.util.TestDataFactory;

import io.restassured.response.Response;

public abstract class BaseApiTest {

    protected BankApiClient api;

    private final Set<Long> customersToCleanup =
            new LinkedHashSet<>();

    @BeforeMethod(alwaysRun = true)
    public void setUpApiClient() {
        api = new BankApiClient();
        customersToCleanup.clear();
    }

    protected void trackCustomer(
            long customerNumber) {

        customersToCleanup.add(customerNumber);
    }

    protected void stopTrackingCustomer(
            long customerNumber) {

        customersToCleanup.remove(customerNumber);
    }

    protected AccountPair createCustomerWithTwoAccounts(
            BigDecimal sourceBalance,
            BigDecimal destinationBalance) {

        long customerNumber =
                TestDataFactory.nextCustomerNumber();

        Response customerResponse =
                api.createCustomer(
                        TestDataFactory.customerPayload(
                                customerNumber,
                                "Chennai"));

        if (customerResponse.statusCode() >= 500) {
            throw new SkipException(
                    "BLOCKED: customer setup endpoint returned HTTP "
                            + customerResponse.statusCode()
                            + ". Transfer scenario cannot be executed "
                            + "without synthetic customer/account setup. "
                            + "Response: "
                            + customerResponse.asString());
        }

        ApiAssertions.status(
                customerResponse,
                TestConfig.getInt(
                        "status.customer.create"),
                "Create test customer");

        trackCustomer(customerNumber);

        long sourceAccount =
                TestDataFactory.nextAccountNumber();

        long destinationAccount =
                TestDataFactory.nextAccountNumber();

        Response sourceResponse =
                api.createAccount(
                        customerNumber,
                        TestDataFactory.accountPayload(
                                sourceAccount,
                                sourceBalance,
                                "AUTO_SOURCE"));

        if (sourceResponse.statusCode() >= 500) {
            throw new SkipException(
                    "BLOCKED: source account setup returned HTTP "
                            + sourceResponse.statusCode()
                            + ". Response: "
                            + sourceResponse.asString());
        }

        ApiAssertions.status(
                sourceResponse,
                TestConfig.getInt(
                        "status.account.create"),
                "Create source account");

        Response destinationResponse =
                api.createAccount(
                        customerNumber,
                        TestDataFactory.accountPayload(
                                destinationAccount,
                                destinationBalance,
                                "AUTO_DESTINATION"));

        if (destinationResponse.statusCode() >= 500) {
            throw new SkipException(
                    "BLOCKED: destination account setup returned HTTP "
                            + destinationResponse.statusCode()
                            + ". Response: "
                            + destinationResponse.asString());
        }

        ApiAssertions.status(
                destinationResponse,
                TestConfig.getInt(
                        "status.account.create"),
                "Create destination account");

        return new AccountPair(
                customerNumber,
                sourceAccount,
                destinationAccount);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpSyntheticCustomers() {

        for (Long customerNumber
                : new LinkedHashSet<>(
                        customersToCleanup)) {

            try {

                Response response =
                        api.deleteCustomer(
                                customerNumber);

                Reporter.log(
                        "[CLEANUP] Customer "
                                + customerNumber
                                + " -> HTTP "
                                + response.statusCode(),
                        true);

            } catch (Exception e) {

                Reporter.log(
                        "[CLEANUP WARNING] Customer "
                                + customerNumber
                                + ": "
                                + e.getMessage(),
                        true);
            }
        }

        customersToCleanup.clear();
    }
}
