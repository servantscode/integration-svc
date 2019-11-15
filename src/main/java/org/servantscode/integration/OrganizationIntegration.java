package org.servantscode.integration;

import java.util.Map;

public class OrganizationIntegration {
    private int id;
    private int systemIntegrationId;
    private String name;
    private Map<String, String> config;
    private int orgId;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSystemIntegrationId() { return systemIntegrationId; }
    public void setSystemIntegrationId(int systemIntegrationId) { this.systemIntegrationId = systemIntegrationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, String> getConfig() { return config; }
    public void setConfig(Map<String, String> config) { this.config = config; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
}
