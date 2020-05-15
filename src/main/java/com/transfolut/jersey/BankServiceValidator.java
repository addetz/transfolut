package com.transfolut.jersey;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Service to take care of input validation.
 */
public class BankServiceValidator {

    private static BankServiceValidator instance;

    private BankServiceValidator() {
        // Exists only to defeat instantiation.
    }

    static BankServiceValidator getInstance() {
        if(instance == null) {
            instance = new BankServiceValidator();
        }
        return instance;
    }

    /**
     * Validates that a given account number is numeric and exists.
     * @param acctNumber - number to validate
     * @return true if valid
     */
    boolean isAccountNumberValid(String acctNumber) {
        return isNumeric(acctNumber);
    }

    /**
     * Validates that a given amount is numeric and positive.
     * @param amount to validate
     * @return error message if invalid or empty if valid
     */
    boolean isAmountValid(String amount) {
        if(!NumberUtils.isCreatable(amount) || new BigDecimal(amount).compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }

    /**
     * Validates that a given currency is a valid {@link org.joda.money.CurrencyUnit}
     * @param currency to validate
     * @return true if valid
     */
    boolean isCurrencyValid(String currency) {
        try {
            CurrencyUnit.of(currency);
            return  true;
        } catch (IllegalCurrencyException e) {
            return false;
        }
    }


    /**
     * Helper method to construct a bad request response
     * @param message message to display
     * @return
     */
    Response getBadRequestResponse(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(message)
                .build();
    }
}
