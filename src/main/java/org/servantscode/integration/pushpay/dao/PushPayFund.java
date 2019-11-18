package org.servantscode.integration.pushpay.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PushPayFund {
    private String key;
    private String name;
    private String code;
    private boolean taxDeductible;
    private String notes;
    private String status;
    @JsonProperty("_links")
    private Map<String, PushPayLink> links;

    // ----- Accessors -----
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isTaxDeductible() { return taxDeductible; }
    public void setTaxDeductible(boolean taxDeductible) { this.taxDeductible = taxDeductible; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, PushPayLink> getLinks() { return links; }
    public void setLinks(Map<String, PushPayLink> links) { this.links = links; }
}
