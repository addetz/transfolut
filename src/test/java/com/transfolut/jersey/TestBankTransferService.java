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
 * Test class for {@link BankTransferService}.
 */
public class TestBankTransferService {

    public static final Money TRANSFER = Money.of(CurrencyUnit.GBP, 1000);
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
    public void testNonNumericFromBankAccount() {
        TransferBean bean = getTransferBean("BLA", String.valueOf(setupTestAccount(TRANSFER)),
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                TransfolutMessages.INVALID_ACCOUNT_NUMBER, Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for non numeric to account number but everything else valid.
     */
    @Test
    public void testNonNumericToBankAccount() {
        TransferBean bean = getTransferBean(String.valueOf(setupTestAccount(TRANSFER)), "BLA",
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                TransfolutMessages.INVALID_ACCOUNT_NUMBER, Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for invalid currency but everything else valid.
     */
    @Test
    public void testInvalidCurrency() {
        TransferBean bean = getTransferBean(String.valueOf(setupTestAccount(TRANSFER)), String.valueOf(setupTestAccount(TRANSFER)),
                "BLA", TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_CURRENCY, bean.currency), Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for non numeric amount but everything else valid.
     */
    @Test
    public void testNonNumericAmount() {
        TransferBean bean = getTransferBean(String.valueOf(setupTestAccount(TRANSFER)), String.valueOf(setupTestAccount(TRANSFER)),
                TRANSFER.getCurrencyUnit().getCurrencyCode(), "BLA");
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_AMOUNT, bean.amount), Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for negative transfer amount but everything else valid.
     */
    @Test
    public void testNegativeAmount() {
        TransferBean bean = getTransferBean(String.valueOf(setupTestAccount(TRANSFER)), String.valueOf(setupTestAccount(TRANSFER)),
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.negated().getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.INVALID_AMOUNT, bean.amount), Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for the bank account numbers being the same but everything else valid.
     */
    @Test
    public void testSameAccounts() {
        String accountNumber = String.valueOf(setupTestAccount(TRANSFER));
        TransferBean bean = getTransferBean(accountNumber, accountNumber,
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                TransfolutMessages.TRANSFER_ACCOUNTS_SAME, Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for non-existent from account but everything else valid.
     */
    @Test
    public void testNonExistentFromAccount() {
        TransferBean bean = getTransferBean( "123456677", String.valueOf(setupTestAccount(TRANSFER)),
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                TransfolutMessages.TRANSFER_ACCOUNTS_NONEXISTENT, Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Test for non-existent to account but everything else valid.
     */
    @Test
    public void testNonExistentToAccount() {
        TransferBean bean = getTransferBean(String.valueOf(setupTestAccount(TRANSFER)), "123456677",
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                TransfolutMessages.TRANSFER_ACCOUNTS_NONEXISTENT, Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Tests that a valid transfer works.
     */
    @Test
    public void testValidTransfer() {
        TransferBean bean = getTransferBean(String.valueOf(setupTestAccount(TRANSFER)), String.valueOf(setupTestAccount(TRANSFER)),
                TRANSFER.getCurrencyUnit().getCurrencyCode(), TRANSFER.getAmount().toString());
        Entity<TransferBean> transferEntity = Entity.entity(bean, MediaType.APPLICATION_JSON);
        Response response = target.path(TransfolutPaths.TRANSFER_URI).request().post(transferEntity);
        checkResponseStatusAndMessage(response,
                String.format(TransfolutMessages.TRANSFER_SUCCESS, TRANSFER.toString(), bean.fromAcct,
                        bean.toAcct), Status.OK.getStatusCode());
        checkBankAccountBalance(bean.fromAcct, Money.of(TRANSFER.getCurrencyUnit(), 0));
        checkBankAccountBalance(bean.toAcct, TRANSFER.multipliedBy(2));
    }
}
