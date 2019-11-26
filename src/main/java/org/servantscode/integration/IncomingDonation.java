package org.servantscode.integration;

import java.time.ZonedDateTime;

public class IncomingDonation {
    private long id;
    private int integrationId;
    private String externalId;
    private String transactionId;
    private String fund;
    private int donorId;
    private String donorName;
    private float amount;
    private ZonedDateTime donationDate;
    private boolean taxDeductible;

    // ----- Accessors -----
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getIntegrationId() { return integrationId; }
    public void setIntegrationId(int integrationId) { this.integrationId = integrationId; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getFund() { return fund; }
    public void setFund(String fund) { this.fund = fund; }

    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }

    public ZonedDateTime getDonationDate() { return donationDate; }
    public void setDonationDate(ZonedDateTime donationDate) { this.donationDate = donationDate; }

    public boolean isTaxDeductible() { return taxDeductible; }
    public void setTaxDeductible(boolean taxDeductible) { this.taxDeductible = taxDeductible; }
}
