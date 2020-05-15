package com.transfolut.jersey;

import com.transfolut.bank.BankService;
import org.joda.money.Money;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Library class for testing bank operations
 */
public class BankUtils {

    /**
     * Helper method to check the expected response message and status
     */
    static void checkResponseStatusAndMessage(Response response, String expectedMessage, int statusCode) {
        String responseMsg = response.readEntity(String.class);
        assertEquals("Incorrect response status", statusCode , response.getStatus());
        assertEquals("Incorrect response message", expectedMessage, responseMsg);
    }

    /**
     * Helper method to create a {@link DepositBean} for testing.
     */
    static DepositBean getDepositBean(String bankAcct, String currencyCode, String depositAmount) {
        DepositBean bean = new DepositBean();
        bean.acct = bankAcct;
        bean.currency = currencyCode;
        bean.amount = depositAmount;
        return bean;
    }


    /**
     * Helper method to check the underlying balance of a given account.
     */
    static void checkBankAccountBalance(String fromAcct, Money expectedAmount) {
        List<String> balances = BankService.getInstance().getAccount(Integer.parseInt(fromAcct)).get().getBalances();
        assertThat("Expected balance should have been found", balances,
                hasItem(expectedAmount.toString()));
    }


    /**
     * Helper method which creates one account just for testing with the deposit amount primary currency
     * and deposits it into it.
     */
    static long setupTestAccount(Money deposit) {
        long testAcctNumber = BankService.getInstance().createAccount(deposit.getCurrencyUnit().getCurrencyCode());
        BankService.getInstance().deposit(testAcctNumber, deposit);
        return testAcctNumber;
    }

    /**
     * Helper method to create a {@link TransferBean} for testing.
     */
    static TransferBean getTransferBean(String fromAcct, String toAcct, String currencyCode, String transferAmount) {
        TransferBean bean = new TransferBean();
        bean.fromAcct = fromAcct;
        bean.toAcct = toAcct;
        bean.currency = currencyCode;
        bean.amount = transferAmount;
        return bean;
    }

    /**
     * Helper method to create a {@link CurrencyBean} for testing.
     */
    static CurrencyBean getCurrencyBean(String currencyCode) {
        CurrencyBean bean = new CurrencyBean();
        bean.currency = currencyCode;
        return bean;
    }

    public static class Worker implements Runnable {
        private final Runnable action;
        private CountDownLatch countDownLatch;

        public Worker(Runnable action, CountDownLatch countDownLatch) {
            this.action = action;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            action.run();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }
    }
}
