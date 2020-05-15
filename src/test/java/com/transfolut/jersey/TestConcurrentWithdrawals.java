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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.transfolut.jersey.BankUtils.*;
import static java.util.stream.Collectors.toList;

public class TestConcurrentWithdrawals {
    public static final int PARALLEL_THREADS = 10;
    public static final int CALL_REPEATS = 50;
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
    public void testConcurrentWithdrawals() throws InterruptedException {
        Money initialDeposit = Money.of(CurrencyUnit.GBP, PARALLEL_THREADS * CALL_REPEATS * 0.9);
        String bankAcct = String.valueOf(setupTestAccount(initialDeposit));
        Money increment = Money.of(CurrencyUnit.GBP, 1);
        DepositBean bean = getDepositBean(bankAcct, increment.getCurrencyUnit().getCurrencyCode(), increment.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        CountDownLatch countDownLatch = new CountDownLatch(PARALLEL_THREADS);

        checkBankAccountBalance(bean.acct, initialDeposit);

        Runnable action = () -> IntStream.range(0, CALL_REPEATS).sequential().forEach(callRepeatNumber -> {
            target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        });

        List<Thread> workers = Stream
                .generate(() -> new Thread(new BankUtils.Worker(action, countDownLatch)))
                .limit(PARALLEL_THREADS)
                .collect(toList());
        workers.forEach(Thread::start);
        countDownLatch.await();

//      No negative balance allowed despite concurrency
        checkBankAccountBalance(bean.acct, Money.zero(CurrencyUnit.GBP));
    }
}
