package org.servantscode.integration.pushpay;

import org.servantscode.integration.IntegrationConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PushpaySystemConfiguration extends IntegrationConfiguration {
    private String appName;
    private String callbackUrl;
    private String clientId;
    private String clientSecret;
    private String authUrl;
    private String apiHost;


    public PushpaySystemConfiguration() {}

    public PushpaySystemConfiguration(Map<String, String> config) {
        setConfiguration(config);
    }

    // ----- Accessors -----
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getApiHost() { return apiHost; }
    public void setApiHost(String apiHost) { this.apiHost = apiHost; }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.appName = configuration.get("appName");
        this.callbackUrl = configuration.get("callbackUrl");
        this.clientId = configuration.get("clientId");
        this.clientSecret = configuration.get("clientSecret");
        this.authUrl = configuration.get("authUrl");
        this.apiHost = configuration.get("apiHost");
    }

    @Override
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>(8);
        map.put("appName", appName);
        map.put("callbackUrl", callbackUrl);
        map.put("clientId", clientId);
        map.put("clientSecret", clientSecret);
        map.put("authUrl", authUrl);
        map.put("apiHost", apiHost);
        return map;
    }
}
