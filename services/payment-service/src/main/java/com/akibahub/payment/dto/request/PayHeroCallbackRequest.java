package com.akibahub.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayHeroCallbackRequest {

    @JsonProperty("status")
    private String status;

    @JsonProperty("external_reference")
    private String externalReference;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("provider_reference")
    private String providerReference;

    @JsonProperty("failure_reason")
    private String failureReason;

    public boolean isSuccessful() {
        return "Success".equalsIgnoreCase(status);
    }
}
