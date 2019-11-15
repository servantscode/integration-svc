package org.servantscode.integration;

public class AvailableIntegration {
    private int id;
    private String name;
    private String authorizationUrl;

    // ----- Private -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAuthorizationUrl() { return authorizationUrl; }
    public void setAuthorizationUrl(String authorizationUrl) { this.authorizationUrl = authorizationUrl; }
}
