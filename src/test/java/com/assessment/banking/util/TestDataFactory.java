package com.assessment.banking.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.assessment.banking.config.TestConfig;

public final class TestDataFactory {

    /*
     * Keep IDs conservative and unique for the shared assessment environment.
     * Customer IDs remain small integer-compatible values.
     */
    private static final long CUSTOMER_SEED =
            100_000L + Math.abs(System.currentTimeMillis() % 700_000L);

    private static final long ACCOUNT_SEED =
            400_000_000_000L
                    + Math.abs(System.nanoTime() % 300_000_000_000L);

    private static final AtomicLong CUSTOMER_SEQUENCE =
            new AtomicLong(CUSTOMER_SEED);

    private static final AtomicLong ACCOUNT_SEQUENCE =
            new AtomicLong(ACCOUNT_SEED);

    private TestDataFactory() {
    }

    public static long nextCustomerNumber() {
        return CUSTOMER_SEQUENCE.incrementAndGet();
    }

    public static long nextAccountNumber() {
        return ACCOUNT_SEQUENCE.incrementAndGet();
    }

    public static long likelyUnknownCustomerNumber() {
        return 1_700_000_000L
                + ThreadLocalRandom.current().nextLong(300_000_000L);
    }

    public static Map<String, Object> customerPayload(
            long customerNumber,
            String city) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put(
                TestConfig.get(
                        "contract.customer.numberField",
                        "customerNumber"),
                customerNumber);

        payload.put(
                TestConfig.get(
                        "contract.customer.firstNameField",
                        "firstName"),
                "AutoTest");

        payload.put(
                TestConfig.get(
                        "contract.customer.lastNameField",
                        "lastName"),
                "User");

        payload.put(
                TestConfig.get(
                        "contract.customer.addressField",
                        "address"),
                "101 Test Street");

        payload.put(
                TestConfig.get(
                        "contract.customer.cityField",
                        "city"),
                city);

        payload.put(
                TestConfig.get(
                        "contract.customer.stateField",
                        "state"),
                "TN");

        /*
         * 10-digit value and below Integer.MAX_VALUE.
         * This avoids both short-number validation and integer overflow.
         */
        long mobileNumber =
                1_300_000_000L
                        + ThreadLocalRandom.current()
                                .nextLong(600_000_000L);

        payload.put(
                TestConfig.get(
                        "contract.customer.mobileField",
                        "mobileNum"),
                mobileNumber);

        payload.put(
                TestConfig.get(
                        "contract.customer.statusField",
                        "status"),
                "ACTIVE");

        return payload;
    }

    public static Map<String, Object> accountPayload(
            long accountNumber,
            BigDecimal openingBalance,
            String accountName) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put(
                TestConfig.get(
                        "contract.account.numberField",
                        "accountNumber"),
                accountNumber);

        payload.put(
                TestConfig.get(
                        "contract.account.typeField",
                        "accout_type"),
                "CURRENT");

        payload.put(
                TestConfig.get(
                        "contract.account.nameField",
                        "account_name"),
                accountName);

        payload.put(
                TestConfig.get(
                        "contract.account.descriptionField",
                        "description"),
                "Automation test account");

        payload.put(
                TestConfig.get(
                        "contract.account.balanceField",
                        "balance"),
                openingBalance);

        payload.put(
                TestConfig.get(
                        "contract.account.interestRateField",
                        "interestRate"),
                new BigDecimal("1.00"));

        return payload;
    }

    public static Map<String, Object> transferPayload(
            long sourceAccount,
            long destinationAccount,
            BigDecimal amount) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put(
                TestConfig.get(
                        "contract.transfer.fromField",
                        "fromAccountNumber"),
                sourceAccount);

        payload.put(
                TestConfig.get(
                        "contract.transfer.toField",
                        "toAccountNumber"),
                destinationAccount);

        payload.put(
                TestConfig.get(
                        "contract.transfer.amountField",
                        "transferAmount"),
                amount);

        return payload;
    }
}
