package org.servantscode.integration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.ZonedDateTime;
import java.util.Map;

public class Integration {
    private int id;
    private int systemIntegrationId;
    private int automationId;
    private String name;
    private String failure;
    private ZonedDateTime lastSync;
    private ZonedDateTime nextScheduledSync;
    private int orgId;

    @JsonIgnore
    private Map<String, String> config;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSystemIntegrationId() { return systemIntegrationId; }
    public void setSystemIntegrationId(int systemIntegrationId) { this.systemIntegrationId = systemIntegrationId; }

    public int getAutomationId() { return automationId; }
    public void setAutomationId(int automationId) { this.automationId = automationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFailure() { return failure; }
    public void setFailure(String failure) { this.failure = failure; }

    public ZonedDateTime getLastSync() { return lastSync; }
    public void setLastSync(ZonedDateTime lastSync) { this.lastSync = lastSync; }

    public ZonedDateTime getNextScheduledSync() { return nextScheduledSync; }
    public void setNextScheduledSync(ZonedDateTime nextScheduledSync) { this.nextScheduledSync = nextScheduledSync; }

    public Map<String, String> getConfig() { return config; }
    public void setConfig(Map<String, String> config) { this.config = config; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
}
