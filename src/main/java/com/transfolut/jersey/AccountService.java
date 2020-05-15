package com.transfolut.jersey;

import com.transfolut.TransfolutMessages;
import com.transfolut.TransfolutPaths;
import com.transfolut.bank.BankAccount;
import com.transfolut.bank.BankService;
import org.joda.money.IllegalCurrencyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Service to fetch accounts.
 */
@Path(TransfolutPaths.ACCOUNTS_URI)
public class AccountService {

    /**
     * Fetches an account given an account number.
     */
    @GET
    @Path("{acctNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccountBalancesByNumber(@PathParam("acctNumber") String acctNumber) {
        if(!BankServiceValidator.getInstance().isAccountNumberValid(acctNumber)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(TransfolutMessages.INVALID_ACCOUNT_NUMBER);
        }

        Optional<BankAccount> bankAccount = BankService.getInstance().getAccount(Integer.parseInt(acctNumber));
        if(!bankAccount.isPresent()) {
            return BankServiceValidator.getInstance().getBadRequestResponse(
                    String.format(TransfolutMessages.ACCOUNT_NOT_FOUND, acctNumber));
        }

        return Response.ok(bankAccount.get().getBalances(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Fetches an account's history given an account number.
     */
    @GET
    @Path("{acctNumber}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccountHistoryByNumber(@PathParam("acctNumber") String acctNumber) {
        if(!BankServiceValidator.getInstance().isAccountNumberValid(acctNumber)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(TransfolutMessages.INVALID_ACCOUNT_NUMBER);
        }

        Optional<BankAccount> bankAccount = BankService.getInstance().getAccount(Integer.parseInt(acctNumber));
        if(!bankAccount.isPresent()) {
            return BankServiceValidator.getInstance().getBadRequestResponse(
                    String.format(TransfolutMessages.ACCOUNT_NOT_FOUND, acctNumber));
        }

        return Response.ok(bankAccount.get().getHistory(), MediaType.APPLICATION_JSON).build();
    }


    /**
     * Fetches all existing accounts
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAccounts() {
        return Response.ok(BankService.getInstance().getBankAccounts(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Creates a new account given a primary currency
     *
     * curl -d '{"currency":"USD"}' -H "Content-Type: application/json" -X POST http://localhost:8080/transfolut/accounts
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewAccount(CurrencyBean currency) {
        if(!BankServiceValidator.getInstance().isCurrencyValid(currency.currency)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(
                    String.format(TransfolutMessages.INVALID_CURRENCY, currency.currency));
        }


        long accountNumber = BankService.getInstance().createAccount(currency.currency);
        return Response.ok(accountNumber, MediaType.APPLICATION_JSON).build();
    }

}
