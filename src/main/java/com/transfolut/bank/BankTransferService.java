package com.transfolut.bank;

import org.joda.money.Money;

/**
 * Transfer service that will move money from one account to another.
 *
 * @author addetz
 */
public class BankTransferService {

    /**
     *
     * @param fromAccount account to transfer from
     * @param toAccount account to transfer to
     * @param amount amount to transfer
     */
    public void transfer(BankAccount fromAccount, BankAccount toAccount, Money amount) {
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
    }
}
