package org.servantscode.integration.pushpay.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.util.Producer;
import org.servantscode.commons.ObjectMapperFactory;
import org.servantscode.commons.client.AbstractServiceClient;
import org.servantscode.integration.pushpay.PushpayClientConfiguration;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;
import org.servantscode.integration.pushpay.dao.GetOrganizationsResponse;
import org.servantscode.integration.pushpay.dao.GetPaymentsResponse;

import javax.validation.constraints.Null;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.servantscode.commons.StringUtils.isEmpty;
import static org.servantscode.commons.StringUtils.isSet;

public class PushpayServiceClient extends AbstractServiceClient {
    private static final Logger LOG = LogManager.getLogger(PushpayServiceClient.class);

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private String token = null;
    private final PushpaySystemConfiguration systemConfig;
    private final PushpayClientConfiguration clientConfig;
    private final Consumer<String> refreshTokenCallback;

    private int backoffStrength = 0;

    public PushpayServiceClient(PushpaySystemConfiguration systemConfig,
                                PushpayClientConfiguration clientConfig,
                                Consumer<String> refreshTokenCallback) {
        super("https://" + systemConfig.getApiHost() + "/v1");
        this.systemConfig = systemConfig;
        this.clientConfig = clientConfig;
        this.refreshTokenCallback = refreshTokenCallback;
    }

    @Override
    public String getReferralUrl() {
        return null;
    }

    @Override
    public String getAuthorization() {
        String authToken = ensureLogin();
        if(isEmpty(token))
            throw new RuntimeException("No access token available.");
        return "Bearer " + authToken;
    }

    public GetOrganizationsResponse getOrganizations() {
        return retryRequest( () -> {
            Response resp = get("/organizations/in-scope");
            handleStatus(resp);
            return parseResponse(resp, GetOrganizationsResponse.class);
        });
    }


    public GetPaymentsResponse getPayments(String orgKey) {
        return retryRequest( () -> {
            Response resp = get("/organization/" + orgKey + "/payments", Collections.singletonMap("count", 100));
            handleStatus(resp);
            return parseResponse(resp, GetPaymentsResponse.class);
        });
    }

    // ----- Private -----
    private String ensureLogin() {
        return isSet(token)? token: login();
    }

    private String login() {
        PushpayAuthClient authClient = new PushpayAuthClient(systemConfig);
        Map<String, String> resp = authClient.refreshBearerToken(clientConfig.getRefreshToken());

        if(!resp.get("refresh_token").equals(clientConfig.getRefreshToken())) {
            LOG.info("Updated refresh token received");
            refreshTokenCallback.accept(resp.get("refresh_token"));
        }

        token = resp.get("access_token");
        LOG.debug("Refreshed bearer token.");
        return token;
    }

    private <T> T retryRequest(Producer<T> doCall) {
        while(true) {
            try {
                T value = doCall.call();
                backoffStrength--;
                return value;
            } catch (NotAuthorizedException e) {
                LOG.info("Login expired. Updating access token.");
                login();
            } catch (TooManyRequestsException e) {
                backoffStrength++;
                LOG.info("Too many requests. Backoff strength is: " + backoffStrength);
                try {
                    Thread.sleep(backoffStrength^2*500);
                } catch (InterruptedException e1) {
                    return null;
                }
            }
        }
    }

    private void handleStatus(Response resp) {
        if (resp.getStatus() == 401) {
            throw new NotAuthorizedException("Invalid credentials.");
        } else if (resp.getStatus() == 429) {
            throw new TooManyRequestsException();
        } else if (resp.getStatus() != 200) {
            throw new RuntimeException("Could not retrieve organization information from PushPay");
        }
    }

    private <T> T parseResponse(Response resp, Class<T> clazz) {
        try {
            String respBody = resp.readEntity(String.class);
            return OBJECT_MAPPER.readValue(respBody, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse json. ", e);
        }
    }
}
