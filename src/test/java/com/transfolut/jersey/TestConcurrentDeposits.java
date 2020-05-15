package com.transfolut.jersey;

import com.transfolut.TransfolutPaths;
import com.transfolut.bank.BankService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.transfolut.jersey.BankUtils.checkBankAccountBalance;
import static com.transfolut.jersey.BankUtils.getDepositBean;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

/**
 * Tests for race conditions in {@link DepositService}.
 */
public class TestConcurrentDeposits {

    public static final int CALL_REPEATS = 50;
    public static final int PARALLEL_THREADS = 10;
    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() {
        server = TransfolutApp.startServer();
        Client c = ClientBuilder.newClient();
        target = c.target(TransfolutPaths.BASE_URI);
    }

    @After
    public void tearDown() {
        BankService.getInstance().clearAccounts();
        server.shutdownNow();
    }

    /**
     * Test concurrent calls and race conditions on the same bank account
     */
    @Test
    public void testConcurrentDeposits() throws InterruptedException {
        long accountNo = BankService.getInstance().createAccount(CurrencyUnit.GBP.getCurrencyCode());
        String bankAcct = String.valueOf(accountNo);
        Money increment = Money.of(CurrencyUnit.GBP, 1);
        DepositBean bean = getDepositBean(bankAcct, increment.getCurrencyUnit().getCurrencyCode(), increment.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        CountDownLatch countDownLatch = new CountDownLatch(PARALLEL_THREADS);

        checkBankAccountBalance(bean.acct, Money.zero(CurrencyUnit.GBP));

        Runnable action = () -> IntStream.range(0, CALL_REPEATS).sequential().forEach(callRepeatNumber -> {
                Response response = target.path(TransfolutPaths.DEPOSIT_URI).request().post(depositEntity);
                assertEquals("Incorrect response status", Response.Status.OK.getStatusCode(), response.getStatus());
        });

        List<Thread> workers = Stream
                .generate(() -> new Thread(new BankUtils.Worker(action, countDownLatch)))
                .limit(PARALLEL_THREADS)
                .collect(toList());
        workers.forEach(Thread::start);
        countDownLatch.await();

        int depositTransactions = BankService.getInstance().getAccount(accountNo).get().getHistory().size() - 1;
        assertEquals("Bank balance is incorrect", increment.multipliedBy(depositTransactions).toString(),
                BankService.getInstance().getAccount(accountNo).get().getBalances().get(0));

    }
}
