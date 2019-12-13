package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.integration.Donor;
import org.servantscode.integration.IncomingDonation;
import org.servantscode.integration.db.DonorDB;
import org.servantscode.integration.db.IncomingDonationDB;
import org.servantscode.integration.donation.DonationRecorder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/integration/donor")
public class DonorSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(DonorSvc.class);

    private DonorDB db;

    public DonorSvc() {
        db = new DonorDB();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Donor> getDonors(@QueryParam("start") @DefaultValue("0") int start,
                                              @QueryParam("count") @DefaultValue("10") int count,
                                              @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                              @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("integration.donor.list");
        try {
            int totalPeople = db.getCount(search);

            List<Donor> results = db.getDonors(search, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving donors failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Donor getDonor(@PathParam("id") int id) {
        verifyUserAccess("integration.donor.read");
        try {
            return db.getDonor(id);
        } catch (Throwable t) {
            LOG.error("Retrieving donor failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Donor createDonor(Donor donor) {
        verifyUserAccess("integration.donor.create");
        try {
            db.create(donor);
            LOG.info("Created donor: " + donor.getName());
            return donor;
        } catch (Throwable t) {
            LOG.error("Creating donor failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Donor updateDonor(Donor donor) {
        verifyUserAccess("integration.donor.update");
        try {
            db.update(donor);
            LOG.info("Edited donor: " + donor.getName());

            checkIncomingDonations(donor);
            return donor;
        } catch (Throwable t) {
            LOG.error("Updating donor failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteDonor(@PathParam("id") int id) {
        verifyUserAccess("integration.donor.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Donor donor = db.getDonor(id);
            if(donor == null || !db.deleteDonor(id))
                throw new NotFoundException();
            LOG.info("Deleted  donor: " + donor.getName());
        } catch (Throwable t) {
            LOG.error("Deleting donor failed:", t);
            throw t;
        }
    }

    // ----- Private -----
    private void checkIncomingDonations(Donor donor) {
        if(donor.getFamilyId() > 0) {
            int count = 0;
            IncomingDonationDB donationDb = new IncomingDonationDB();
            List<IncomingDonation> donations = donationDb.getDonationsForDonor(donor.getId());
            DonationRecorder recorder = new DonationRecorder();
            for(IncomingDonation donation: donations) {
                recorder.record(donation);
                donationDb.deleteIncomingDonation(donation.getId());
                count++;
            }

            LOG.info("Recorded " + count + " incoming donations.");
        }
    }
}
