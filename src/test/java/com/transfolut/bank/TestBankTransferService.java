package com.transfolut.bank;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Test class for the {@link BankTransferService}.
 *
 * @author addetz
 */
public class TestBankTransferService {

    /**
     * Tests the transfer function moves funds between {@link BankAccount} correctly.
     */
    @Test
    public void testTransfer() {
        CurrencyUnit primaryCurrency = CurrencyUnit.GBP;
        Money deposit = Money.of(primaryCurrency, 1000);
        Money unrelatedDeposit = Money.of(CurrencyUnit.EUR, 750);

        BankAccount fromAccount = new BankAccount(11111111, primaryCurrency);
        BankAccount toAccount = new BankAccount(22222222, primaryCurrency);
        BankTransferService transferService = new BankTransferService();
        fromAccount.deposit(deposit);
        fromAccount.deposit(unrelatedDeposit);

        transferService.transfer(fromAccount, toAccount, deposit);

        assertThat("The from account should now only contain unrelated deposit.", fromAccount.getBalances(),
                containsInAnyOrder(Money.zero(primaryCurrency).toString(), unrelatedDeposit.toString()));
        assertThat("The to account should now have the full balance", toAccount.getBalances(),
                containsInAnyOrder(deposit.toString()));
    }



}
