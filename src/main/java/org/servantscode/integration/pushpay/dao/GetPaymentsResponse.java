package org.servantscode.integration.pushpay.dao;

public class GetPaymentsResponse extends PushPayPaginatedResponse<PushPayPayment> {
    private String organizationKey;

    // ----- Accessors -----
    public String getOrganizationKey() { return organizationKey; }
    public void setOrganizationKey(String organizationKey) { this.organizationKey = organizationKey; }
}
