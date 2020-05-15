package com.transfolut.bank;

import com.transfolut.bank.BankAccount;
import com.transfolut.bank.BankService;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;

/**
 * Test class for {@link BankService}.
 *
 * @author addetz
 */
public class TestBankService {

    public static final CurrencyUnit PRIMARY_CURRENCY = CurrencyUnit.GBP;
    private static final Money DEPOSIT = Money.of(PRIMARY_CURRENCY, 1000);
    private BankService bankService;
    private BankAccount bankAccount;

    @Before
    public void setup() {
        bankService = BankService.getInstance();
        long acctNumber = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());
        bankAccount = bankService.getAccount(acctNumber).get();
    }

    /**
     * Tests creating a new bank account.
     */
    @Test
    public void testCreateBankAccount() {
        long newAccount = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());

        assertThat("The initial and new bank account should be contained in the list of bank accounts", bankService.getBankAccounts(),
                hasItems(newAccount, bankAccount.getBankAccountNumber()));
    }

    /**
     * Tests creating a new bank account with illegal currency fails.
     */
    @Test(expected = IllegalCurrencyException.class)
    public void testCreateBankAccountIllegalCurrency() {
        bankService.createAccount("AddetzBitcoin");
    }

    /**
     * Tests that finding {@link BankAccount} using its account number works as expected
     */
    @Test
    public void testGetAccount() {
        long initialBankAcctNumber = bankAccount.getBankAccountNumber();
        assertTrue("Initial bank account should be present",
                bankService.getAccount(initialBankAcctNumber).isPresent());
        assertFalse("Another bank account number should not be present",
                bankService.getAccount(initialBankAcctNumber * 2).isPresent());
        assertEquals("Initial bank account should have been found", bankAccount,
                bankService.getAccount(initialBankAcctNumber).get());
    }


    /**
     * Basic test case testing that bank account deposits into the correct acct only.
     */
    @Test
    public void testDeposit() {
        long newAccountNumber = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());
        BankAccount newAccount = bankService.getAccount(newAccountNumber).get();

        bankService.deposit(bankAccount.getBankAccountNumber(), DEPOSIT);

        assertThat("The new account should have 0 balances only", newAccount.getBalances(),
                containsInAnyOrder(Money.zero(PRIMARY_CURRENCY).toString()));
        assertThat("One updated balance of GBP should exist", bankAccount.getBalances(),
                containsInAnyOrder(DEPOSIT.toString()));

    }

    /**
     * Basic test case testing that an exception is thrown for non existent bank accts.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDepositIllegalBankAcct() {
        bankService.deposit(bankAccount.getBankAccountNumber() * 2, DEPOSIT);
    }

    /**
     * Basic test case testing that insufficient funds withdrawals are not allowed
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithdrawInsufficient() {
        bankService.withdraw(bankAccount.getBankAccountNumber(), DEPOSIT);
    }

    /**
     * Basic test case testing that withdrawals are drawn from the correct account only
     */
    @Test
    public void testWithdraw() {
        long newAccountNumber = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());
        BankAccount newAccount = bankService.getAccount(newAccountNumber).get();

        bankService.deposit(bankAccount.getBankAccountNumber(), DEPOSIT);
        bankService.withdraw(bankAccount.getBankAccountNumber(), DEPOSIT);

        assertThat("The new account should have 0 balances only", newAccount.getBalances(),
                containsInAnyOrder(Money.zero(PRIMARY_CURRENCY).toString()));
        assertThat("One updated balance of GBP should exist", bankAccount.getBalances(),
                containsInAnyOrder(Money.zero(PRIMARY_CURRENCY).toString()));
    }

    /**
     * Tests that an exception is thrown for withdrawals from non existent bank accts.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithdrawIllegalBankAcct() {
        bankService.withdraw(bankAccount.getBankAccountNumber() * 2, DEPOSIT);
    }


    /**
     * Tests that money is transferred between accts correctly.
     */
    @Test
    public void testTransfer() {
        long newAccountNumber = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());
        BankAccount newAccount = bankService.getAccount(newAccountNumber).get();
        bankAccount.deposit(DEPOSIT);

        bankService.transfer(bankAccount.getBankAccountNumber(), newAccount.getBankAccountNumber(), DEPOSIT);

        assertThat("Initial bank acct has negative balance", bankAccount.getBalances(),
                containsInAnyOrder(Money.zero(DEPOSIT.getCurrencyUnit()).toString()));
        assertThat("New bank acct has positive balance", newAccount.getBalances(),
                containsInAnyOrder(DEPOSIT.toString()));

    }

    /**
     * Tests that an exception is thrown for transfers from non existent bank acct.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTransferIllegalFromBankAcct() {
        long newAccountNumber = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());
        bankService.getAccount(newAccountNumber).get();

        bankService.transfer(bankAccount.getBankAccountNumber() * 2, newAccountNumber, DEPOSIT);
    }

    /**
     * Tests that an exception is thrown for transfers to non existent bank acct.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTransferIllegalToBankAcct() {
        long newAccountNumber = bankService.createAccount(PRIMARY_CURRENCY.getCurrencyCode());
        bankService.getAccount(newAccountNumber).get();

        bankService.transfer(bankAccount.getBankAccountNumber(), newAccountNumber * 2, DEPOSIT);
    }
}
