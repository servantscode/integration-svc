package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.integration.IncomingDonation;
import org.servantscode.integration.db.IncomingDonationDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/integration/donation")
public class IncomingDonationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(IncomingDonationSvc.class);

    private IncomingDonationDB db;

    public IncomingDonationSvc() {
        db = new IncomingDonationDB();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<IncomingDonation> getIncomingDonations(@QueryParam("start") @DefaultValue("0") int start,
                                                                    @QueryParam("count") @DefaultValue("10") int count,
                                                                    @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                                    @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("integration.donation.list");
        try {
            int totalPeople = db.getCount(search);

            List<IncomingDonation> results = db.getIncomingDonations(search, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving incomingDonations failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public IncomingDonation getIncomingDonation(@PathParam("id") int id) {
        verifyUserAccess("integration.donation.read");
        try {
            return db.getIncomingDonation(id);
        } catch (Throwable t) {
            LOG.error("Retrieving incomingDonation failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public IncomingDonation createIncomingDonation(IncomingDonation incomingDonation) {
        verifyUserAccess("integration.donation.create");
        try {
            db.create(incomingDonation);
            LOG.info("Created incomingDonation: " + incomingDonation.getTransactionId());
            return incomingDonation;
        } catch (Throwable t) {
            LOG.error("Creating incomingDonation failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public IncomingDonation updateIncomingDonation(IncomingDonation incomingDonation) {
        verifyUserAccess("integration.donation.update");
        try {
            db.update(incomingDonation);
            LOG.info("Edited incomingDonation: " + incomingDonation.getTransactionId());
            return incomingDonation;
        } catch (Throwable t) {
            LOG.error("Updating incomingDonation failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteIncomingDonation(@PathParam("id") int id) {
        verifyUserAccess("integration.donation.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            IncomingDonation incomingDonation = db.getIncomingDonation(id);
            if(incomingDonation == null || !db.deleteIncomingDonation(id))
                throw new NotFoundException();
            LOG.info("Deleted  incomingDonation: " + incomingDonation.getTransactionId());
        } catch (Throwable t) {
            LOG.error("Deleting incomingDonation failed:", t);
            throw t;
        }
    }
}
