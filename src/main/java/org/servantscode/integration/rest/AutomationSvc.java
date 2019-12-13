package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.integration.Automation;
import org.servantscode.integration.IncomingDonation;
import org.servantscode.integration.db.AutomationDB;
import org.servantscode.integration.db.IncomingDonationDB;
import org.servantscode.integration.donation.DonationRecorder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/integration/automation")
public class AutomationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(AutomationSvc.class);

    private AutomationDB db;

    public AutomationSvc() {
        db = new AutomationDB();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Automation> getAutomations(@QueryParam("start") @DefaultValue("0") int start,
                                              @QueryParam("count") @DefaultValue("10") int count,
                                              @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                              @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("integration.automation.list");
        try {
            int totalPeople = db.getCount(search);

            List<Automation> results = db.getAutomations(search, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving automations failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Automation getAutomation(@PathParam("id") int id) {
        verifyUserAccess("integration.automation.read");
        try {
            return db.getAutomation(id);
        } catch (Throwable t) {
            LOG.error("Retrieving automation failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Automation createAutomation(Automation automation) {
        verifyUserAccess("integration.automation.create");
        try {
            db.create(automation);
            LOG.info("Created automation: " + automation.getId());
            return automation;
        } catch (Throwable t) {
            LOG.error("Creating automation failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Automation updateAutomation(Automation automation) {
        verifyUserAccess("integration.automation.update");
        try {
            db.update(automation);
            LOG.info("Edited automation: " + automation.getId());

            return automation;
        } catch (Throwable t) {
            LOG.error("Updating automation failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteAutomation(@PathParam("id") int id) {
        verifyUserAccess("integration.automation.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Automation automation = db.getAutomation(id);
            if(automation == null || !db.deleteAutomation(id))
                throw new NotFoundException();
            LOG.info("Deleted  automation: " + automation.getId());
        } catch (Throwable t) {
            LOG.error("Deleting automation failed:", t);
            throw t;
        }
    }
}
