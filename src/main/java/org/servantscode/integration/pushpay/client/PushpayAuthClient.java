package org.servantscode.integration.pushpay.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Map;

public class PushpayAuthClient extends BasePushpayClient {
    private static final Logger LOG = LogManager.getLogger(PushpayAuthClient.class);

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
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>(4);
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", config.getCallbackUrl());
        LOG.debug("Retrieving access tokens from PushPay. Using callback URL: " + config.getCallbackUrl());

        return retryRequest( () -> {
            Response response = buildInvocation().post(Entity.form(params));
            handleStatus(response);
            return parseResponse(response, new TypeReference<Map<String, String>>() {});
        });
    }

    public Map<String, String> refreshBearerToken(String refreshToken) {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>(4);
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        return retryRequest( () -> {
            Response response =  buildInvocation().post(Entity.form(params));
            handleStatus(response);
            return parseResponse(response, new TypeReference<Map<String, String>>() {});
        });
    }
}
