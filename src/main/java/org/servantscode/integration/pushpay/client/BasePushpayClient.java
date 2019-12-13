package org.servantscode.integration.pushpay.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.util.Producer;
import org.servantscode.commons.ObjectMapperFactory;
import org.servantscode.commons.client.AbstractServiceClient;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class BasePushpayClient extends AbstractServiceClient {
    private static final Logger LOG = LogManager.getLogger(BasePushpayClient.class);
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private int backoffStrength = 0;

    protected BasePushpayClient(String baseUrl) {
        super(baseUrl);
    }

    public String login() { return null; }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return Collections.emptyMap();
    }

    protected <T> T retryRequest(Producer<T> doCall) {
        while(true) {
            try {
                T value = doCall.call();
                backoffStrength--;
                return value;
            } catch (NotAuthorizedException e) {
                LOG.info("Authorization expired. Reauthenticating.");
                if(login() == null)
                    throw new NotAuthorizedException("Could not authenticate with remote server.", e);
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

    protected void handleStatus(Response resp) {
        if (resp.getStatus() == 401) {
            throw new NotAuthorizedException("Invalid credentials.");
        } else if (resp.getStatus() == 429) {
            //TODO: Use time on failure request + 1 sec.
            throw new TooManyRequestsException();
        } else if (resp.getStatus() != 200) {
            throw new RuntimeException("Could not retrieve organization information from PushPay. Returned status: " + resp.getStatus());
        }
    }

    protected <T> T parseResponse(Response resp, Class<T> clazz) {
        try {
            String respBody = resp.readEntity(String.class);
            return OBJECT_MAPPER.readValue(respBody, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse json. ", e);
        }
    }

    protected <T> T parseResponse(Response resp, TypeReference<T> typeRef) {
        try {
            String respBody = resp.readEntity(String.class);
            return OBJECT_MAPPER.readValue(respBody, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse json. ", e);
        }
    }
}
