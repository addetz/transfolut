
package com.transfolut.jersey;

import com.transfolut.TransfolutMessages;
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
import javax.ws.rs.core.Response.Status;

import static com.transfolut.jersey.BankUtils.*;

/**
 * Test class for {@link WithdrawService}.
 */
public class TestWithdrawService {

    public static final Money INITIAL_DEPOSIT = Money.of(CurrencyUnit.GBP, 1000);
    public static final Money DEPOSIT = Money.of(CurrencyUnit.GBP, 350);
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
     * Test for non numeric from account number but everything else valid.
     */
    @Test
    public void testNonNumericBankAccount() {
        DepositBean bean = getDepositBean("BLA", DEPOSIT.getCurrencyUnit().getCurrencyCode(),
                DEPOSIT.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response, TransfolutMessages.INVALID_ACCOUNT_NUMBER, Status.BAD_REQUEST.getStatusCode());
    }


    /**
     * Test for invalid currency but everything else valid.
     */
    @Test
    public void testInvalidCurrency() {
        DepositBean bean = getDepositBean(String.valueOf(setupTestAccount(INITIAL_DEPOSIT)),
                "BLA", DEPOSIT.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_CURRENCY, bean.currency), Status.BAD_REQUEST.getStatusCode() );
    }

    /**
     * Test for non numeric amount but everything else valid.
     */
    @Test
    public void testNonNumericAmount() {
        DepositBean bean = getDepositBean(String.valueOf(setupTestAccount(INITIAL_DEPOSIT)),
                DEPOSIT.getCurrencyUnit().getCurrencyCode(), "BLA");
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_AMOUNT, bean.amount), Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for negative transfer amount but everything else valid.
     */
    @Test
    public void testNegativeAmount() {
        DepositBean bean = getDepositBean(String.valueOf(setupTestAccount(INITIAL_DEPOSIT)),
                DEPOSIT.getCurrencyUnit().getCurrencyCode(), DEPOSIT.negated().getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_AMOUNT, bean.amount),Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for non-existent account but everything else valid.
     */
    @Test
    public void testNonExistentAccount() {
        DepositBean bean = getDepositBean("123456677",
                DEPOSIT.getCurrencyUnit().getCurrencyCode(), DEPOSIT.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.ACCOUNT_NOT_FOUND, bean.acct),Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Tests that a valid withdraw works.
     */
    @Test
    public void testValidWithdraw() {
        DepositBean bean = getDepositBean(String.valueOf(setupTestAccount(INITIAL_DEPOSIT)),
                DEPOSIT.getCurrencyUnit().getCurrencyCode(), DEPOSIT.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.WITHDRAW_SUCCESS, DEPOSIT.toString(), bean.acct), Status.OK.getStatusCode());
        checkBankAccountBalance(bean.acct, INITIAL_DEPOSIT.minus(DEPOSIT));
    }

    /**
     * Tests that a valid withdraw works.
     */
    @Test
    public void testWithdrawInsufficient() {
        Money withdraw = INITIAL_DEPOSIT.multipliedBy(2);
        DepositBean bean = getDepositBean(String.valueOf(setupTestAccount(INITIAL_DEPOSIT)),
                INITIAL_DEPOSIT.getCurrencyUnit().getCurrencyCode(), withdraw.getAmount().toString());
        Entity<DepositBean> depositEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.WITHDRAW_URI).request().post(depositEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INSUFFICIENT_BALANCE, withdraw.toString()), Status.BAD_REQUEST.getStatusCode());
        checkBankAccountBalance(bean.acct, INITIAL_DEPOSIT);
    }
}