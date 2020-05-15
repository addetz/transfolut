package com.transfolut.bank;

import com.transfolut.bank.BankAccount;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link BankAccount}.
 *
 * @author addetz
 */
public class TestBankAccount {

    private static final CurrencyUnit INITIAL_CURRENCY = CurrencyUnit.GBP;
    private static final long BANK_ACCT_NUMBER = 12345678L;
    private static final Money INITIAL_BALANCE = Money.of(INITIAL_CURRENCY, 1000);
    private static final Money DEPOSIT = Money.of(CurrencyUnit.EUR, 350);
    private BankAccount bankAccount;

    @Before
    public void setup() {
        bankAccount = new BankAccount(BANK_ACCT_NUMBER, INITIAL_CURRENCY);
        bankAccount.deposit(INITIAL_BALANCE);
    }

    /**
     * Tests fetching the bank acct number and histories of the newly created {@link BankAccount}.
     */
    @Test
    public void testNewBankAcct() {
        assertEquals("Bank account number incorrect", BANK_ACCT_NUMBER, bankAccount.getBankAccountNumber());
        assertThat("Primary currency zero balance initialized", bankAccount.getBalances(),
                containsInAnyOrder(INITIAL_BALANCE.toString()));
        assertThat("Account creation and transaction history should exist", bankAccount.getHistory(),
                containsInAnyOrder(containsString("Bank account created."),
                        containsString("Transaction of " + INITIAL_BALANCE.toString())));
    }


    /**
     * Tests depositing money in existing currency updates balances correctly.
     */
    @Test
    public void testDepositExistingCurrency() {
        Money deposit = Money.of(INITIAL_CURRENCY, 700);
        bankAccount.deposit(deposit);

        assertThat("One updated balance of GBP should exist", bankAccount.getBalances(),
                containsInAnyOrder(INITIAL_BALANCE.plus(deposit).toString()));
        assertThat("Account creation and transaction history should exist", bankAccount.getHistory(),
                containsInAnyOrder(containsString("Bank account created."),
                        containsString("Transaction of " + INITIAL_BALANCE.toString()),
                        containsString("Transaction of " + deposit.toString())));
    }

    /**
     * Tests depositing money in a new currency inserts a new balance.
     */
    @Test
    public void testDepositNewCurrency() {
        bankAccount.deposit(DEPOSIT);

        assertThat("Initial balance of GBP and new balance of EUR should exist", bankAccount.getBalances(),
                containsInAnyOrder(INITIAL_BALANCE.toString(), DEPOSIT.toString()));
        assertThat("Account creation and transaction history should exist", bankAccount.getHistory(),
                containsInAnyOrder(containsString("Bank account created."),
                        containsString("Transaction of " + INITIAL_BALANCE.toString()),
                        containsString("Transaction of " + DEPOSIT.toString())));
    }


    /**
     * Tests withdrawing money in existing currency updates balances correctly.
     */
    @Test
    public void testWithdrawExistingCurrency() {
        Money withdrawal = Money.of(INITIAL_CURRENCY, 400);
        bankAccount.withdraw(withdrawal);

        assertThat("One updated balance of GBP should exist", bankAccount.getBalances(),
                containsInAnyOrder(INITIAL_BALANCE.minus(withdrawal).toString()));
        assertThat("Account creation and transaction history should exist", bankAccount.getHistory(),
                containsInAnyOrder(containsString("Bank account created."),
                        containsString("Transaction of " + INITIAL_BALANCE.toString()),
                        containsString("Transaction of " + withdrawal.negated().toString())));
    }

    /**
     * Test that attempting to withdraw more than current balance throws error
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsufficientBalance() {
        bankAccount.withdraw(INITIAL_BALANCE.multipliedBy(2));
    }

    /**
     * Tests withdrawing full amount in existing currency drops balance entirely
     * and leaves a zero amount for primary currency only
     */
    @Test
    public void testWithdrawFullExistingCurrencyMainCurrency() {
        bankAccount.deposit(DEPOSIT);
        bankAccount.withdraw(DEPOSIT);
        bankAccount.withdraw(INITIAL_BALANCE);

        assertThat("A 0 GBP amount should be found only for initial currency", bankAccount.getBalances(),
                containsInAnyOrder(Money.zero(INITIAL_CURRENCY).toString()));
        assertThat("Account creation and transaction history should exist", bankAccount.getHistory(),
                containsInAnyOrder(containsString("Bank account created."),
                        containsString("Transaction of " + INITIAL_BALANCE.toString()),
                        containsString("Transaction of " + DEPOSIT.toString()),
                        containsString("Transaction of " + DEPOSIT.negated().toString()),
                        containsString("Transaction of " + INITIAL_BALANCE.negated().toString())));
    }

    /**
     * Tests withdrawing money in a new currency throws exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithdrawNewCurrency() {
        bankAccount.withdraw(DEPOSIT);
    }
}
