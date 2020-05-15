package com.transfolut.jersey;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON bean for passing currency code for account creation.
 *
 * @author addetz
 */
class CurrencyBean {
    @JsonProperty
    String currency;
}