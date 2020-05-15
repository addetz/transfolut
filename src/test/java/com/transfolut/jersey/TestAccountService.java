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

import static com.transfolut.jersey.BankUtils.checkResponseStatusAndMessage;
import static com.transfolut.jersey.BankUtils.getCurrencyBean;
import static com.transfolut.jersey.BankUtils.setupTestAccount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the {@link AccountService} REST endpoints.
 */
public class TestAccountService {

    public static final Money DEPOSIT = Money.of(CurrencyUnit.GBP, 1000);
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
     * Test for getting multiple account numbers.
     */
    @Test
    public void testGetAccounts() {
        long initialAcctNumber = setupTestAccount(DEPOSIT);
        long eurAcctNumber = setupTestAccount(Money.of(CurrencyUnit.EUR, 2000));
        Response response = target.path(TransfolutPaths.ACCOUNTS_URI).request().get();
        String responseMsg = response.readEntity(String.class);
        assertEquals("Status should have been OK", Status.OK.getStatusCode(), response.getStatus());
        assertTrue("Initial acct number should have been returned", responseMsg.contains(String.valueOf(initialAcctNumber)));
        assertTrue("Second acct number should have been returned", responseMsg.contains(String.valueOf(eurAcctNumber)));
    }

    /**
     * Test for getting the account history of a single account
     */
    @Test
    public void testGetHistory() {
        long initialAcctNumber = setupTestAccount(DEPOSIT);
        Response response = target.path(String.format("%s/%s/history", TransfolutPaths.ACCOUNTS_URI, initialAcctNumber)).request().get();
        String responseMsg = response.readEntity(String.class);
        assertEquals("Status should have been OK", Status.OK.getStatusCode(), response.getStatus());
        assertTrue("History should contain creation", responseMsg.contains("created"));
        assertTrue("History should contain deposit", responseMsg.contains("Transaction"));
    }

    /**
     * Test for getting empty account list.
     */
    @Test
    public void testGetAccountsEmpty() {
        Response response = target.path(TransfolutPaths.ACCOUNTS_URI).request().get();
        checkResponseStatusAndMessage(response, "[]", Status.OK.getStatusCode());
    }

    /**
     * Test for getting a valid bank account balance.
     */
    @Test
    public void testValidBankAccount() {
        long initialAcctNumber = setupTestAccount(DEPOSIT);
        Response response = target.path(String.format("%s/%s", TransfolutPaths.ACCOUNTS_URI, initialAcctNumber)).request().get();
        String responseMsg = response.readEntity(String.class);
        assertEquals("Status should have been OK", Status.OK.getStatusCode(), response.getStatus());
        assertEquals("One balance should of initial deposit amount should have been returned",
                String.format("[\"%s\"]", DEPOSIT.toString()), responseMsg);
    }

    /**
     * Test for getting an invalid bank account balance.
     */
    @Test
    public void testInvalidBankAccount() {
        long invalidAcct = setupTestAccount(DEPOSIT) * 2;
        Response response = target.path(String.format("%s/%s", TransfolutPaths.ACCOUNTS_URI, invalidAcct)).request().get();
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.ACCOUNT_NOT_FOUND, invalidAcct), Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for getting an non numeric acct number.
     */
    @Test
    public void testNonNumericBankAccount() {
        Response response = target.path(String.format("%s/%s", TransfolutPaths.ACCOUNTS_URI, "BLA")).request().get();
        checkResponseStatusAndMessage(response, TransfolutMessages.INVALID_ACCOUNT_NUMBER, Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for creating a new bank account with illegal currency
     */
    @Test
    public void testInvalidCurrencyCreateAccount() {
        CurrencyBean bean = getCurrencyBean("BLA");
        Entity<CurrencyBean> currencyBeanEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.ACCOUNTS_URI).request().post(currencyBeanEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_CURRENCY, bean.currency), Status.BAD_REQUEST.getStatusCode() );
    }

    /**
     * Test for creating a new bank account with existing currency
     */
    @Test
    public void testCreateAccount() {
        CurrencyBean bean = getCurrencyBean("USD");
        Entity<CurrencyBean> currencyBeanEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.ACCOUNTS_URI).request().post(currencyBeanEntity);
        String responseMsg = response.readEntity(String.class);
        assertEquals("Status should have been OK", Status.OK.getStatusCode(), response.getStatus());
        assertTrue("A bank account number should have been returned", responseMsg.length() > 0);
    }
}