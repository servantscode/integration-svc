package org.servantscode.integration.pushpay.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class GetFundsResponse {
    private PushPayFund defaultFund;
    private List<PushPayFund> funds;
    @JsonProperty("_links")
    private Map<String, PushPayLink> links;

    // ----- Accessors -----
    public PushPayFund getDefaultFund() { return defaultFund; }
    public void setDefaultFund(PushPayFund defaultFund) { this.defaultFund = defaultFund; }

    public List<PushPayFund> getFunds() { return funds; }
    public void setFunds(List<PushPayFund> funds) { this.funds = funds; }

    public Map<String, PushPayLink> getLinks() { return links; }
    public void setLinks(Map<String, PushPayLink> links) { this.links = links; }
}
