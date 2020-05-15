package com.transfolut.jersey;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON bean for passing bank account numbers and amount for deposits
 *
 * @author addetz
 */
class DepositBean {
    @JsonProperty
    String acct;
    @JsonProperty
    String currency;
    @JsonProperty
    String amount;
}