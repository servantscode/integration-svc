package org.servantscode.integration.pushpay.dao;

public class PushPayMerchant {
    private String homeCountry;
    private String visibility;
    private String status;
    private int version;
    private String key;
    private String handle;
    private String name;
    private String address;

    // ----- Accessors -----
    public String getHomeCountry() { return homeCountry; }
    public void setHomeCountry(String homeCountry) { this.homeCountry = homeCountry; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
