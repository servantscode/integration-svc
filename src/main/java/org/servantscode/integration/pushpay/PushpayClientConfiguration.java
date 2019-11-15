package org.servantscode.integration.pushpay;

import org.servantscode.integration.IntegrationConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PushpayClientConfiguration extends IntegrationConfiguration {
    private String refreshToken;

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        refreshToken = configuration.get("refreshToken");
    }

    @Override
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("refreshToken", refreshToken);
        return map;
    }

    // ----- Accessors -----
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
