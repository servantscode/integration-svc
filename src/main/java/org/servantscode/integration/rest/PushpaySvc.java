package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.Integration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.IntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;
import org.servantscode.integration.donation.RecordDonationProcess;
import org.servantscode.integration.pushpay.PushpayClientConfiguration;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;
import org.servantscode.integration.pushpay.client.PushpayServiceClient;
import org.servantscode.integration.pushpay.dao.GetOrganizationsResponse;
import org.servantscode.integration.pushpay.dao.GetPaymentsResponse;
import org.servantscode.integration.pushpay.dao.PushPayOrganization;
import org.servantscode.integration.pushpay.dao.PushPayPayment;

import javax.validation.constraints.Null;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@Path("/integration/pushpay")
public class PushpaySvc extends SCServiceBase {
    public static final String PUSH_PAY = "PushPay";

    private static final Logger LOG = LogManager.getLogger(PushpaySvc.class);

    private IntegrationDB orgIntDb;
    private SystemIntegrationDB sysIntDb;

    public PushpaySvc() {
        this.orgIntDb = new IntegrationDB();
        this.sysIntDb = new SystemIntegrationDB();
    }

    @POST @Path("/payment/sync") @Produces(MediaType.APPLICATION_JSON)
    public void syncPayments() {

        String orgName = OrganizationContext.getOrganization().getHostName();
        Integration orgInt = orgIntDb.getIntegration(PUSH_PAY, orgName);
        ZonedDateTime syncTime = ZonedDateTime.now();

        PushpayServiceClient client = configureClient(orgInt);

        try {
            List<PushPayOrganization> orgs = client.getOrganizations().getItems();
            List<PushPayPayment> payments = new LinkedList<>();
            for (PushPayOrganization org : orgs) {
                GetPaymentsResponse resp = client.getPayments(org.getKey());
                payments.addAll(resp.getItems());
            }

            RecordDonationProcess proc = new RecordDonationProcess(payments, OrganizationContext.getOrganization(), orgInt.getId());
            //TODO: Async this.
            proc.run();

            orgInt.setLastSync(syncTime);
            orgInt.setFailure(null);
            orgIntDb.update(orgInt);
        } catch(Throwable t) {
            LOG.error("Failed to sync donation data with PushPah.", t);
            orgInt.setFailure(t.getMessage());
            orgIntDb.update(orgInt);
        }
    }

    // ----- Private -----
    private PushpayServiceClient configureClient(Integration orgInt) {
        SystemIntegration sysInt = sysIntDb.getSystemIntegrationByName(PUSH_PAY);

        return new PushpayServiceClient(new PushpaySystemConfiguration(sysInt.getConfig()),
                                        new PushpayClientConfiguration(orgInt.getConfig()),
                                        (String refreshToken) -> {
                                            orgInt.getConfig().put("refreshToken", refreshToken);
                                            orgIntDb.update(orgInt);
                                        });
    }
}
