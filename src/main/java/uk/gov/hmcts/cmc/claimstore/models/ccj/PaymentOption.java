package uk.gov.hmcts.cmc.claimstore.models.ccj;

public enum PaymentOption {
    IMMEDIATELY("immediately"),
    FULL("on or before a set date"),
    INSTALMENTS("by instalments");

    String description;

    PaymentOption(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
