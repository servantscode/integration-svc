package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.Automation;
import org.servantscode.integration.pushpay.PushPaySynchronizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/integration/pushpay")
public class PushpaySvc extends SCServiceBase {
    public static final String PUSH_PAY = "PushPay";

    private static final Logger LOG = LogManager.getLogger(PushpaySvc.class);

    public PushpaySvc() {
    }

    @POST @Path("/payment/sync") @Produces(APPLICATION_JSON)
    public void syncPayments() {
        new PushPaySynchronizer().synchronize(OrganizationContext.orgId());
    }
}
