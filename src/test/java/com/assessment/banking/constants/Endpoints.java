package com.assessment.banking.constants;

public final class Endpoints {

    public static final String ADD_CUSTOMER =
            "/customers/add";

    public static final String CUSTOMER_BY_NUMBER =
            "/customers/{customerNumber}";

    public static final String ADD_ACCOUNT =
            "/accounts/add/{customerNumber}";

    public static final String ACCOUNT_BY_NUMBER =
            "/accounts/{accountNumber}";

    public static final String TRANSFER =
            "/accounts/transfer/{customerNumber}";

    public static final String TRANSACTIONS =
            "/accounts/transactions/{accountNumber}";

    private Endpoints() {
    }
}
