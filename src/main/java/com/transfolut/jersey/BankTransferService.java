package com.transfolut.jersey;

import com.transfolut.TransfolutMessages;
import com.transfolut.TransfolutPaths;
import com.transfolut.bank.BankService;
import org.apache.commons.lang3.math.NumberUtils;
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

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Service to transfer money between accounts.
 */
@Path(TransfolutPaths.TRANSFER_URI)
public class BankTransferService {
    /**
     * Creates a new account given a primary currency
     * curl -d '{"fromAccount": "125", "toAccount": "124", "currency":"USD", "amount": "500"}' -H "Content-Type: application/json" -X POST http://localhost:8080/transfolut/transfer
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(TransferBean transferParams) {
        if(!BankServiceValidator.getInstance().isAccountNumberValid(transferParams.fromAcct) ||
                !BankServiceValidator.getInstance().isAccountNumberValid(transferParams.toAcct)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(TransfolutMessages.INVALID_ACCOUNT_NUMBER);
        }

        if(!BankServiceValidator.getInstance().isAmountValid(transferParams.amount)) {
            return BankServiceValidator.getInstance().getBadRequestResponse
                    (String.format(TransfolutMessages.INVALID_AMOUNT, transferParams.amount));
        }

        if(!BankServiceValidator.getInstance().isCurrencyValid(transferParams.currency)) {
            return BankServiceValidator.getInstance().getBadRequestResponse
                    (String.format(TransfolutMessages.INVALID_CURRENCY, transferParams.currency));
        }

        //Validate that the two account numbers are not the same
        if(transferParams.fromAcct.equals(transferParams.toAcct)) {
            return BankServiceValidator.getInstance().getBadRequestResponse(TransfolutMessages.TRANSFER_ACCOUNTS_SAME);
        }

        try {
            CurrencyUnit currency = CurrencyUnit.of(transferParams.currency);
            Money transferAmount = Money.of(currency, new BigDecimal(transferParams.amount));
            BankService.getInstance().transfer(Integer.parseInt(transferParams.fromAcct),
                    Integer.parseInt(transferParams.toAcct), transferAmount);
            return Response.ok(
                    String.format(TransfolutMessages.TRANSFER_SUCCESS, transferAmount.toString(), transferParams.fromAcct,
                            transferParams.toAcct), MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            return BankServiceValidator.getInstance().getBadRequestResponse(e.getMessage());
        }
    }
}

