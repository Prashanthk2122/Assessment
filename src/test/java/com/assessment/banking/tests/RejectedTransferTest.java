package com.assessment.banking.tests;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.assessment.banking.assertion.ApiAssertions;
import com.assessment.banking.base.BaseApiTest;
import com.assessment.banking.config.TestConfig;
import com.assessment.banking.model.AccountPair;
import com.assessment.banking.util.JsonSupport;
import com.assessment.banking.util.TestDataFactory;

import io.restassured.response.Response;

public class RejectedTransferTest
        extends BaseApiTest {

    @DataProvider(name = "nonPositiveAmounts")
    public Object[][] nonPositiveAmounts() {

        return new Object[][] {
                { new BigDecimal("0.00") },
                { new BigDecimal("-10.00") }
        };
    }

    @Test(
            dataProvider = "nonPositiveAmounts",
            description =
                    "Reject non-positive transfer with HTTP 400 "
                    + "without balance or successful-history changes.")
    public void nonPositiveTransferIsRejectedWithoutSideEffects(
            BigDecimal rejectedAmount) {

        AccountPair accounts =
                createCustomerWithTwoAccounts(
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00"));

        verifyRejectedTransferHasNoSideEffects(
                accounts,
                rejectedAmount);
    }

    @Test(description =
            "Reject a transfer exceeding available balance "
            + "without balance or successful-history changes.")
    public void insufficientFundsTransferIsRejectedWithoutSideEffects() {

        AccountPair accounts =
                createCustomerWithTwoAccounts(
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00"));

        verifyRejectedTransferHasNoSideEffects(
                accounts,
                new BigDecimal("500.01"));
    }

    private void verifyRejectedTransferHasNoSideEffects(
            AccountPair accounts,
            BigDecimal rejectedAmount) {

        Response sourceBeforeResponse =
                api.getAccount(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceBeforeResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read source account before rejected transfer");

        Response destinationBeforeResponse =
                api.getAccount(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationBeforeResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read destination account before rejected transfer");

        BigDecimal sourceBefore =
                JsonSupport.decimal(
                        sourceBeforeResponse,
                        "balance");

        BigDecimal destinationBefore =
                JsonSupport.decimal(
                        destinationBeforeResponse,
                        "balance");

        BigDecimal historyAmount =
                rejectedAmount.abs();

        Response sourceHistoryBefore =
                api.getTransactions(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceHistoryBefore,
                200,
                "Read source history before rejected transfer");

        Response destinationHistoryBefore =
                api.getTransactions(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationHistoryBefore,
                200,
                "Read destination history before rejected transfer");

        int debitCountBefore =
                JsonSupport.countSuccessfulTransactions(
                        sourceHistoryBefore,
                        "DEBIT",
                        historyAmount);

        int creditCountBefore =
                JsonSupport.countSuccessfulTransactions(
                        destinationHistoryBefore,
                        "CREDIT",
                        historyAmount);

        Response transferResponse =
                api.transfer(
                        accounts.customerNumber(),
                        TestDataFactory.transferPayload(
                                accounts.sourceAccountNumber(),
                                accounts.destinationAccountNumber(),
                                rejectedAmount));

        ApiAssertions.status(
                transferResponse,
                TestConfig.getInt(
                        "status.transfer.reject"),
                "Reject invalid transfer");

        ApiAssertions.meaningfulError(
                transferResponse,
                "Rejected transfer");

        Response sourceAfterResponse =
                api.getAccount(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceAfterResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read source account after rejected transfer");

        Response destinationAfterResponse =
                api.getAccount(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationAfterResponse,
                TestConfig.getInt(
                        "status.account.get"),
                "Read destination account after rejected transfer");

        ApiAssertions.moneyEquals(
                sourceBefore,
                JsonSupport.decimal(
                        sourceAfterResponse,
                        "balance"),
                "Rejected transfer must not change source balance.");

        ApiAssertions.moneyEquals(
                destinationBefore,
                JsonSupport.decimal(
                        destinationAfterResponse,
                        "balance"),
                "Rejected transfer must not change destination balance.");

        Response sourceHistoryAfter =
                api.getTransactions(
                        accounts.sourceAccountNumber());

        ApiAssertions.status(
                sourceHistoryAfter,
                200,
                "Read source history after rejected transfer");

        Response destinationHistoryAfter =
                api.getTransactions(
                        accounts.destinationAccountNumber());

        ApiAssertions.status(
                destinationHistoryAfter,
                200,
                "Read destination history after rejected transfer");

        int debitCountAfter =
                JsonSupport.countSuccessfulTransactions(
                        sourceHistoryAfter,
                        "DEBIT",
                        historyAmount);

        int creditCountAfter =
                JsonSupport.countSuccessfulTransactions(
                        destinationHistoryAfter,
                        "CREDIT",
                        historyAmount);

        Assert.assertEquals(
                debitCountAfter,
                debitCountBefore,
                "Rejected transfer must not add a successful debit.");

        Assert.assertEquals(
                creditCountAfter,
                creditCountBefore,
                "Rejected transfer must not add a successful credit.");
    }
}
