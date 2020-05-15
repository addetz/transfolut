package com.transfolut;

/**
 * List of error messages that the API can return.
 */
public interface TransfolutMessages {
    String INVALID_ACCOUNT_NUMBER = "Account numbers must be numeric.";
    String ACCOUNT_NOT_FOUND = "Bank account not found for supplied account number %s.";
    String INVALID_CURRENCY = "No currency found for currency code %s.";
    String HISTORY_ACCOUNT_CREATED = "%s : Bank account created.";
    String HISTORY_TRANSACTION = "%s : Transaction of %s";
    String INVALID_AMOUNT = "Amount must be a positive number, but was %s.";
    String TRANSFER_SUCCESS = "Transfer of %s between account %s and account %s was successful.";
    String DEPOSIT_SUCCESS = "Deposit of %s in account %s was successful.";
    String WITHDRAW_SUCCESS = "Withdrawal of %s in account %s was successful.";
    String TRANSFER_ACCOUNTS_NONEXISTENT = "Accounts to transfer between are non-existent.";
    String TRANSFER_ACCOUNTS_SAME = "The accounts to transfer between cannot be the same.";
    String INSUFFICIENT_BALANCE = "Insufficient balance for withdrawal of %s";
}

