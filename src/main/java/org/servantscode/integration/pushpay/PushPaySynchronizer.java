package org.servantscode.integration.pushpay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.Integration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.IntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;
import org.servantscode.integration.donation.RecordDonationProcess;
import org.servantscode.integration.pushpay.client.PushpayServiceClient;
import org.servantscode.integration.pushpay.dao.GetPaymentsResponse;
import org.servantscode.integration.pushpay.dao.PushPayOrganization;
import org.servantscode.integration.pushpay.dao.PushPayPayment;
import org.servantscode.integration.rest.PushpaySvc;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.servantscode.integration.rest.PushpaySvc.PUSH_PAY;

public class PushPaySynchronizer {
    private static final Logger LOG = LogManager.getLogger(PushpaySvc.class);

    private IntegrationDB orgIntDb;
    private SystemIntegrationDB sysIntDb;

    public PushPaySynchronizer() {
        this.orgIntDb = new IntegrationDB();
        this.sysIntDb = new SystemIntegrationDB();
    }

    public void synchronize(int orgId) {
        Integration orgInt = orgIntDb.getIntegration(PUSH_PAY, orgId);
        ZonedDateTime syncTime = ZonedDateTime.now();

        PushpayServiceClient client = configureClient(orgInt);

        try {
            List<PushPayOrganization> orgs = client.getOrganizations().getItems();
            List<PushPayPayment> payments = new LinkedList<>();
            for (PushPayOrganization org : orgs) {
                GetPaymentsResponse resp = client.getPayments(org.getKey(), 0, orgInt.getLastSync(), syncTime);
                payments.addAll(resp.getItems());

                int pages = resp.getTotalPages();
                for(int i = 1; i<pages; i++) {
                    GetPaymentsResponse page = client.getPayments(org.getKey(), i, orgInt.getLastSync(), syncTime);
                    payments.addAll(page.getItems());
                }
            }

            //TODO: Async this.
            RecordDonationProcess proc = new RecordDonationProcess(payments, orgId, orgInt.getId());
            proc.run();

            orgInt.setLastSync(syncTime);
            orgInt.setFailure(null);
            orgIntDb.update(orgInt);
        } catch(Throwable t) {
            LOG.error("Failed to sync donation data with PushPah.", t);
            orgInt.setFailure(t.getMessage());
            orgIntDb.update(orgInt);
            throw t;
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
