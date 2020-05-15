package com.transfolut.jersey;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON bean for passing bank account numbers and amount for bank transfer
 *
 * @author addetz
 */
class TransferBean {
    @JsonProperty
    String fromAcct;
    @JsonProperty
    String toAcct;
    @JsonProperty
    String currency;
    @JsonProperty
    String amount;
}