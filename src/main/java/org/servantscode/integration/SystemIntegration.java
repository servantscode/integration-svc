package org.servantscode.integration;

import java.util.Map;

public class SystemIntegration {
    private int id;
    private String name;
    private Map<String, String> config;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, String> getConfig() { return config; }
    public void setConfig(Map<String, String> config) { this.config = config; }
}
