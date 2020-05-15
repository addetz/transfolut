package com.transfolut.jersey;

import com.transfolut.TransfolutMessages;
import com.transfolut.TransfolutPaths;
import com.transfolut.bank.BankService;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

/**
 * Service to handle bank account withdrawals from accounts.
 */
@Path(TransfolutPaths.WITHDRAW_URI)
public class WithdrawService {
    /**
     * Creates a new account given a primary currency
     * curl -d '{"acct": "125", "currency":"USD", "amount": "500"}' -H "Content-Type: application/json" -X POST http://localhost:8080/transfolut/withdraw
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response withdraw(DepositBean deposit) {
        if(!BankServiceValidator.getInstance().isAccountNumberValid(deposit.acct)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(
                    TransfolutMessages.INVALID_ACCOUNT_NUMBER);
        }

        if(!BankServiceValidator.getInstance().isAmountValid(deposit.amount)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(
                    String.format(TransfolutMessages.INVALID_AMOUNT, deposit.amount));
        }

        if(!BankServiceValidator.getInstance().isCurrencyValid(deposit.currency)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(
                    String.format(TransfolutMessages.INVALID_CURRENCY, deposit.currency));
        }

        try {
            CurrencyUnit currency = CurrencyUnit.of(deposit.currency);
            Money depositAmount = Money.of(currency, new BigDecimal(deposit.amount));
            BankService.getInstance().withdraw(Integer.parseInt(deposit.acct), depositAmount);
            return Response.ok(
                    String.format(TransfolutMessages.WITHDRAW_SUCCESS, depositAmount.toString(), deposit.acct),
                    MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            return BankServiceValidator.getInstance().getBadRequestResponse(e.getMessage());
        }
    }
}
