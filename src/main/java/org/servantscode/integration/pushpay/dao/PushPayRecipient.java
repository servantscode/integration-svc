package org.servantscode.integration.pushpay.dao;

public class PushPayRecipient {
    private String key;
    private String name;
    private String role;

    // ----- Accessors -----
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
