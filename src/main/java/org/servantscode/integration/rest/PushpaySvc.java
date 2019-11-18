package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.OrganizationIntegration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.OrganizationIntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;
import org.servantscode.integration.donation.RecordDonationProcess;
import org.servantscode.integration.pushpay.PushpayClientConfiguration;
import org.servantscode.integration.pushpay.PushpaySystemConfiguration;
import org.servantscode.integration.pushpay.client.PushpayServiceClient;
import org.servantscode.integration.pushpay.dao.GetOrganizationsResponse;
import org.servantscode.integration.pushpay.dao.GetPaymentsResponse;
import org.servantscode.integration.pushpay.dao.PushPayOrganization;
import org.servantscode.integration.pushpay.dao.PushPayPayment;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;

@Path("/integration/pushpay")
public class PushpaySvc extends SCServiceBase {
    public static final String PUSH_PAY = "PushPay";

    private static final Logger LOG = LogManager.getLogger(PushpaySvc.class);

    private OrganizationIntegrationDB orgIntDb;
    private SystemIntegrationDB sysIntDb;

    public PushpaySvc() {
        this.orgIntDb = new OrganizationIntegrationDB();
        this.sysIntDb = new SystemIntegrationDB();
    }

    @GET @Path("/organization") @Produces(MediaType.APPLICATION_JSON)
    public List<PushPayOrganization> getOrgs() {

        PushpayServiceClient client = configureClient();

        GetOrganizationsResponse resp = client.getOrganizations();
        return resp.getItems();
    }

    @GET @Path("/payment") @Produces(MediaType.APPLICATION_JSON)
    public List<PushPayPayment> getPayments() {

        PushpayServiceClient client = configureClient();

        List<PushPayOrganization> orgs = client.getOrganizations().getItems();
        List<PushPayPayment> payments = new LinkedList<>();
        for(PushPayOrganization org: orgs) {
            GetPaymentsResponse resp = client.getPayments(org.getKey());
            payments.addAll(resp.getItems());
        }
        return payments;
    }

    @POST @Path("/payment/sync") @Produces(MediaType.APPLICATION_JSON)
    public void syncPayments() {

        PushpayServiceClient client = configureClient();

        List<PushPayOrganization> orgs = client.getOrganizations().getItems();
        List<PushPayPayment> payments = new LinkedList<>();
        for(PushPayOrganization org: orgs) {
            GetPaymentsResponse resp = client.getPayments(org.getKey());
            payments.addAll(resp.getItems());
        }

        RecordDonationProcess proc = new RecordDonationProcess(payments, OrganizationContext.getOrganization());
        //TODO: Async this.
        proc.run();

    }

    // ----- Private -----
    private PushpayServiceClient configureClient() {
        String orgName = OrganizationContext.getOrganization().getHostName();
        SystemIntegration sysInt = sysIntDb.getSystemIntegrationByName(PUSH_PAY);
        OrganizationIntegration orgInt = orgIntDb.getOrganizationIntegration(PUSH_PAY, orgName);

        return new PushpayServiceClient(new PushpaySystemConfiguration(sysInt.getConfig()),
                new PushpayClientConfiguration(orgInt.getConfig()));
    }
}
