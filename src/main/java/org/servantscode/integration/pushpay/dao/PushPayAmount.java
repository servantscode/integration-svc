package org.servantscode.integration.pushpay.dao;

public class PushPayAmount {
    private String amount;
    private String currency;

    // ----- Accessors -----
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
