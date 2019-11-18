package org.servantscode.integration.pushpay.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.ObjectMapperFactory;
import org.servantscode.commons.client.AbstractServiceClient;
import org.servantscode.integration.OrganizationIntegration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.OrganizationIntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;
import org.servantscode.integration.pushpay.PushpayClientConfiguration;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;
import org.servantscode.integration.pushpay.dao.GetOrganizationsResponse;
import org.servantscode.integration.pushpay.dao.GetPaymentsResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

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

    public PushpayServiceClient(PushpaySystemConfiguration systemConfig, PushpayClientConfiguration clientConfig) {
        super("https://" + systemConfig.getApiHost() + "/v1");
        this.systemConfig = systemConfig;
        this.clientConfig = clientConfig;
    }

    @Override
    public String getReferralUrl() {
        return null;
    }

    @Override
    public String getAuthorization() {
        String authToken = login();
        if(isEmpty(token))
            throw new RuntimeException("No access token available.");
        return "Bearer " + authToken;
    }

    public GetOrganizationsResponse getOrganizations() {
        Response resp = get("/organizations/in-scope");

        if(resp.getStatus() != 200)
            throw new RuntimeException("Could not retrieve organization information from PushPay");

        String respBody = resp.readEntity(String.class);
        LOG.debug("Got response\n" + respBody);
//        GetOrganizationsResponse orgResp = resp.readEntity(GetOrganizationsResponse.class);
        GetOrganizationsResponse orgResp = null;
        try {
            orgResp = OBJECT_MAPPER.readValue(respBody, GetOrganizationsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse json. ", e);
        }
        return orgResp;
    }

    public GetPaymentsResponse getPayments(String orgKey) {
        Response resp = get("/organization/" + orgKey + "/payments");

        if(resp.getStatus() != 200)
            throw new RuntimeException("Could not retrieve payment information from PushPay");

        String respBody = resp.readEntity(String.class);
        LOG.debug("Got response\n" + respBody);
        GetPaymentsResponse paymentResp = null;
        try {
            paymentResp = OBJECT_MAPPER.readValue(respBody, GetPaymentsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse json. ", e);
        }
//        GetPaymentsResponse paymentResp = resp.readEntity(GetPaymentsResponse.class);
        return paymentResp;
    }

    // ----- Private -----
    private String login() {
        if(isSet(token))
            return token;

        PushpayAuthClient authClient = new PushpayAuthClient(systemConfig);

//        OrganizationIntegrationDB orgIntDb = new OrganizationIntegrationDB();
//        OrganizationIntegration orgInt = orgIntDb.getOrganizationIntegration("PushPay", orgPrefix);
//        PushpayClientConfiguration config = new PushpayClientConfiguration();
//        config.setConfiguration(orgInt.getConfig());
        Map<String, String> resp = authClient.refreshBearerToken(clientConfig.getRefreshToken());

        if(!resp.get("refresh_token").equals(clientConfig.getRefreshToken())) {
            LOG.info("Updated refresh token received");//for " + orgInt.getName() + " integration for org: " + orgInt.getOrgId());
//            config.setRefreshToken(resp.get("refresh_token"));
//            orgInt.setConfig(config.toMap());
//            orgIntDb.updateOrganizationIntegration(orgInt);
        }

        token = resp.get("access_token");
        LOG.debug("Refreshed bearer token.");
        return token;
    }
}
