package com.assessment.banking.tests;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.assessment.banking.assertion.ApiAssertions;
import com.assessment.banking.base.BaseApiTest;
import com.assessment.banking.config.TestConfig;
import com.assessment.banking.model.AccountPair;
import com.assessment.banking.util.JsonSupport;
import com.assessment.banking.util.TestDataFactory;

import io.restassured.response.Response;

public class SuccessfulTransferTest
        extends BaseApiTest {

    @Test(description =
            "Transfer a positive amount and verify response, "
            + "both balances, debit history and credit history.")
    public void successfulTransferMovesExactAmountAndCreatesHistory() {

        BigDecimal transferAmount =
                new BigDecimal("125.25");

        AccountPair accounts =
                createCustomerWithTwoAccounts(
                        new BigDecimal("1000.00"),
                        new BigDecimal("250.00"));

        Response sourceBeforeResponse =
                api.getAccount(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceBeforeResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read source account before transfer");

        Response destinationBeforeResponse =
                api.getAccount(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationBeforeResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read destination account before transfer");

        BigDecimal sourceBefore =
                JsonSupport.decimal(
                        sourceBeforeResponse,
                        "balance");

        BigDecimal destinationBefore =
                JsonSupport.decimal(
                        destinationBeforeResponse,
                        "balance");

        Response sourceHistoryBefore =
                api.getTransactions(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceHistoryBefore,
                200,
                "Read source history before transfer");

        Response destinationHistoryBefore =
                api.getTransactions(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationHistoryBefore,
                200,
                "Read destination history before transfer");

        int debitCountBefore =
                JsonSupport.countSuccessfulTransactions(
                        sourceHistoryBefore,
                        "DEBIT",
                        transferAmount);

        int creditCountBefore =
                JsonSupport.countSuccessfulTransactions(
                        destinationHistoryBefore,
                        "CREDIT",
                        transferAmount);

        Response transferResponse =
                api.transfer(
                        accounts.customerNumber(),
                        TestDataFactory.transferPayload(
                                accounts.sourceAccountNumber(),
                                accounts.destinationAccountNumber(),
                                transferAmount));

        ApiAssertions.status(
                transferResponse,
                TestConfig.getInt(
                        "status.transfer.success"),
                "Perform successful transfer");

        Response sourceAfterResponse =
                api.getAccount(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceAfterResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read source account after transfer");

        Response destinationAfterResponse =
                api.getAccount(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationAfterResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read destination account after transfer");

        BigDecimal sourceAfter =
                JsonSupport.decimal(
                        sourceAfterResponse,
                        "balance");

        BigDecimal destinationAfter =
                JsonSupport.decimal(
                        destinationAfterResponse,
                        "balance");

        ApiAssertions.moneyEquals(
                sourceBefore.subtract(transferAmount),
                sourceAfter,
                "Source balance should decrease by exact amount.");

        ApiAssertions.moneyEquals(
                destinationBefore.add(transferAmount),
                destinationAfter,
                "Destination balance should increase by exact amount.");

        Response sourceHistoryAfter =
                api.getTransactions(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceHistoryAfter,
                200,
                "Read source history after transfer");

        Response destinationHistoryAfter =
                api.getTransactions(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationHistoryAfter,
                200,
                "Read destination history after transfer");

        int debitCountAfter =
                JsonSupport.countSuccessfulTransactions(
                        sourceHistoryAfter,
                        "DEBIT",
                        transferAmount);

        int creditCountAfter =
                JsonSupport.countSuccessfulTransactions(
                        destinationHistoryAfter,
                        "CREDIT",
                        transferAmount);

        Assert.assertEquals(
                debitCountAfter,
                debitCountBefore + 1,
                "Exactly one new successful debit record is expected.");

        Assert.assertEquals(
                creditCountAfter,
                creditCountBefore + 1,
                "Exactly one new successful credit record is expected.");
    }
}
