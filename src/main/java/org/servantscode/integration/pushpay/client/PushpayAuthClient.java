package org.servantscode.integration.pushpay.client;

import org.servantscode.commons.client.AbstractServiceClient;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Map;

public class PushpayAuthClient extends AbstractServiceClient {

    private final PushpaySystemConfiguration config;

    public PushpayAuthClient(PushpaySystemConfiguration config) {
        super("https://auth.pushpay.com/" + config.getAppName() + "/oauth/token");
        this.config = config;
    }

    public PushpayAuthClient(Map<String, String> configMap) {
        super("https://auth.pushpay.com/" + configMap.get("appName") + "/oauth/token");
        config = new PushpaySystemConfiguration();
        config.setConfiguration(configMap);
    }

    @Override
    public String getReferralUrl() {
        return null;
    }

    @Override
    public String getAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString((config.getClientId() + ":" + config.getClientSecret()).getBytes());
    }

    public Map<String, String> getInitialAccess(String code) {
        String callbackUrl = config.getCallbackUrl();
        try {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>(4);
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("redirect_uri", callbackUrl);

            Response response =  buildInvocation().post(Entity.form(params));

            if(response.getStatus() != 200)
                throw new RuntimeException("Failed to retrieve new bearer token from authorization code. Response code: " + response.getStatus());

            return response.readEntity(new GenericType<Map<String, String>>(){});
        } catch (Throwable e) {
            throw new RuntimeException("Failed to retrieve new bearer token from authorization code.", e);
        }
    }

    public Map<String, String> refreshBearerToken(String refreshToken) {
        try {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>(4);
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);

            Response response =  buildInvocation().post(Entity.form(params));

            if(response.getStatus() != 200)
                throw new RuntimeException("Failed to retrieve new bearer token from refresh token. Response code: " + response.getStatus());

            return response.readEntity(new GenericType<Map<String, String>>(){});

        } catch (Throwable e) {
            throw new RuntimeException("Failed to retrieve new bearer token from refresh token", e);
        }
    }
}
