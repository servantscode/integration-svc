package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.Organization;
import org.servantscode.commons.db.OrganizationDB;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.OrganizationIntegration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.OrganizationIntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;
import org.servantscode.integration.pushpay.PushpayClientConfiguration;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;
import org.servantscode.integration.pushpay.client.PushpayAuthClient;
import org.servantscode.integration.pushpay.client.PushpayServiceClient;
import org.servantscode.integration.pushpay.dao.GetOrganizationsResponse;
import org.servantscode.integration.pushpay.dao.PushPayOrganization;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.servantscode.commons.StringUtils.isEmpty;
import static org.servantscode.commons.StringUtils.isSet;

@Path("/pushpay")
public class PushpayCallbackSvc extends SCServiceBase {
    public static final String PUSH_PAY = "PushPay";

    private static final Logger LOG = LogManager.getLogger(PushpayCallbackSvc.class);

    private OrganizationIntegrationDB orgIntDb;
    private SystemIntegrationDB sysIntDb;
    private OrganizationDB orgDb;

    public PushpayCallbackSvc() {
        this.orgIntDb = new OrganizationIntegrationDB();
        this.sysIntDb = new SystemIntegrationDB();
        this.orgDb = new OrganizationDB();
    }


    @GET @Path("/finish_auth") @Produces(TEXT_PLAIN)
    public String finishAuthentication(@QueryParam("code") String code,
                                       @QueryParam("error") String error,
                                       @QueryParam("state") String orgPrefix) {

        if(isSet(error) || isEmpty(code))
            failureRedirect(orgPrefix);

        SystemIntegration sysInt = getSystemIntegration(orgPrefix);

        try {
            Map<String, String> resp = getAuthorization(code, sysInt);
            String authToken = resp.get("access_token");
            String refreshToken = resp.get("refresh_token");

            saveOrgIntegration(orgPrefix, sysInt, refreshToken);

            return "Great success! Auth token is: " + authToken + " and refresh token is: " + refreshToken;
//                throw new RedirectionException(301, URI.create("https;//" + orgPrefix + ".servantscode.org/integrations/pushpay/success"));
        } catch(RedirectionException re) {
            throw re;
        } catch(Throwable t) {
            LOG.error("Failed to authenticate with PushPay.", t);
            failureRedirect(orgPrefix);
        }
        return null; //Won't happen
    }

    // ----- Private -----
    private void saveOrgIntegration(@QueryParam("state") String orgPrefix, SystemIntegration sysInt, String refreshToken) {
        Map<String, String> config = new HashMap<>(2);
        config.put("refreshToken", refreshToken);

        OrganizationIntegration existingInt = orgIntDb.getOrganizationIntegration(PUSH_PAY, "orgPrefix");
        if(existingInt != null) {
            existingInt.setConfig(config);
            orgIntDb.updateOrganizationIntegration(existingInt);
        } else {
            Organization org = orgDb.getOrganization(orgPrefix);
            if(org == null)
                failureRedirect(orgPrefix);

            OrganizationIntegration orgInt = new OrganizationIntegration();
            orgInt.setSystemIntegrationId(sysInt.getId());
            orgInt.setConfig(config);
            orgInt.setOrgId(org.getId());
            orgIntDb.create(orgInt);
        }
    }

    private SystemIntegration getSystemIntegration(@QueryParam("state") String orgPrefix) {
        try {
            SystemIntegration sysInt = sysIntDb.getSystemIntegrationByName(PUSH_PAY);
            if(sysInt != null)
                return sysInt;
        } catch(Throwable t) {
            LOG.error("Could not retrieve system integration.", t);
        }
        failureRedirect(orgPrefix);
        return null; //won't happen
    }

    private Map<String, String> getAuthorization(@QueryParam("code") String code, SystemIntegration sysInt) {
        PushpaySystemConfiguration systemConfig = new PushpaySystemConfiguration();
        systemConfig.setConfiguration(sysInt.getConfig());
        PushpayAuthClient client = new PushpayAuthClient(systemConfig);
        return client.getInitialAccess(code);
    }

    private void failureRedirect(String orgPrefix) {
        throw new RedirectionException(301, URI.create("https;//" + orgPrefix + ".servantscode.org/integrations/pushpay/failed"));
    }

    //Util to inject refresh token into dev environment
//    public static void main(String[] args) throws IOException {
//        PushpaySvc svc = new PushpaySvc();
//        SystemIntegration integration = svc.getSystemIntegration(PUSH_PAY);
//
//        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("Which org? [localhost]");
//        String orgPrefix = input.readLine().trim();
//        orgPrefix = isSet(orgPrefix)? orgPrefix: "localhost";
//
//        System.out.println("Refresh Token? []");
//        String refreshToken = input.readLine().trim();
//        if(isEmpty(refreshToken)) {
//            System.err.println("No refresh token input.");
//            System.exit(1);
//        }
//
//        svc.saveOrgIntegration(orgPrefix, integration, refreshToken);
//    }
}
