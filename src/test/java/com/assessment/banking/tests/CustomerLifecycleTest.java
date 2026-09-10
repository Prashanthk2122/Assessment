package com.assessment.banking.tests;

import java.math.BigDecimal;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.assessment.banking.assertion.ApiAssertions;
import com.assessment.banking.base.BaseApiTest;
import com.assessment.banking.config.TestConfig;
import com.assessment.banking.util.JsonSupport;
import com.assessment.banking.util.TestDataFactory;

import io.restassured.response.Response;

public class CustomerLifecycleTest extends BaseApiTest {

    @Test(description =
            "Create, retrieve, update, retrieve again, delete a customer, "
            + "then verify customer and associated account are unavailable.")
    public void customerLifecyclePersistsUpdateAndCascadesDelete() {

        long customerNumber =
                TestDataFactory.nextCustomerNumber();

        Map<String, Object> createPayload =
                TestDataFactory.customerPayload(
                        customerNumber,
                        "Chennai");

        /*
         * STEP 1 - Create customer
         */
        Response createResponse =
                api.createCustomer(createPayload);

        ApiAssertions.status(
                createResponse,
                TestConfig.getInt(
                        "status.customer.create"),
                "Create customer");

        trackCustomer(customerNumber);

        /*
         * STEP 2 - Retrieve customer
         */
        Response firstGet =
                api.getCustomer(customerNumber);

        ApiAssertions.status(
                firstGet,
                TestConfig.getInt(
                        "status.customer.get"),
                "Retrieve newly created customer");

        Assert.assertEquals(
                JsonSupport.text(
                        firstGet,
                        "city"),
                "Chennai",
                "Created customer city should persist.");

        Assert.assertEquals(
                JsonSupport.text(
                        firstGet,
                        "status"),
                "ACTIVE",
                "Created customer status should persist.");

        /*
         * STEP 3 - Create associated account
         */
        long accountNumber =
                TestDataFactory.nextAccountNumber();

        Response createAccountResponse =
                api.createAccount(
                        customerNumber,
                        TestDataFactory.accountPayload(
                                accountNumber,
                                new BigDecimal("300.00"),
                                "AUTO_LIFECYCLE"));

        ApiAssertions.status(
                createAccountResponse,
                TestConfig.getInt(
                        "status.account.create"),
                "Create associated account");

        /*
         * STEP 4 - Update customer
         */
        Map<String, Object> updatePayload =
                TestDataFactory.customerPayload(
                        customerNumber,
                        "Coimbatore");

        updatePayload.put(
                TestConfig.get(
                        "contract.customer.addressField",
                        "address"),
                "202 Persisted State Avenue");

        Response updateResponse =
                api.updateCustomer(
                        customerNumber,
                        updatePayload);

        ApiAssertions.status(
                updateResponse,
                TestConfig.getInt(
                        "status.customer.update"),
                "Update customer");

        /*
         * STEP 5 - Retrieve again and verify update
         */
        Response secondGet =
                api.getCustomer(customerNumber);

        ApiAssertions.status(
                secondGet,
                TestConfig.getInt(
                        "status.customer.get"),
                "Retrieve customer after update");

        Assert.assertEquals(
                JsonSupport.text(
                        secondGet,
                        "city"),
                "Coimbatore",
                "Updated city must persist.");

        Assert.assertEquals(
                JsonSupport.text(
                        secondGet,
                        "address"),
                "202 Persisted State Avenue",
                "Updated address must persist.");

        /*
         * STEP 6 - Delete customer
         */
        Response deleteResponse =
                api.deleteCustomer(customerNumber);

        ApiAssertions.status(
                deleteResponse,
                TestConfig.getInt(
                        "status.customer.delete"),
                "Delete customer");

        stopTrackingCustomer(customerNumber);

        /*
         * STEP 7 - Verify deleted customer is unavailable
         */
        Response deletedCustomerResponse =
                api.getCustomer(customerNumber);

        ApiAssertions.absent(
                deletedCustomerResponse,
                TestConfig.getInt(
                        "status.customer.absent"),
                "Retrieve deleted customer");

        /*
         * STEP 8 - Verify associated account is also unavailable
         */
        Response deletedAccountResponse =
                api.getAccount(accountNumber);

        ApiAssertions.status(
                deletedAccountResponse,
                TestConfig.getInt(
                        "status.account.notFound"),
                "Retrieve account after customer deletion");

        ApiAssertions.meaningfulError(
                deletedAccountResponse,
                "Deleted associated account lookup");
    }
}