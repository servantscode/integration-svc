package org.servantscode.integration.donation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.Donor;
import org.servantscode.integration.IncomingDonation;
import org.servantscode.integration.db.DataReconciler;
import org.servantscode.integration.db.DonorDB;

import java.util.HashMap;
import java.util.Map;

public class DonationRecorder {
    private static Logger LOG = LogManager.getLogger(DonationRecorder.class);


    private DonorDB donorDb;
    private DataReconciler dataReconciler;

    public DonationRecorder() {
        this(OrganizationContext.orgId());
    }

    public DonationRecorder(int orgId) {
        donorDb = new DonorDB();
        dataReconciler = new DataReconciler(orgId);
    }

    public void record(IncomingDonation donation) {
        this.record(donorDb.getDonor(donation.getDonorId()), donation);
    }

    public void record(Donor donor, IncomingDonation donation) {
        if(donor == null)
            throw new IllegalStateException("Cannot record a donation without donor information.");

        int fundId = dataReconciler.getFundIdForName(donation.getFund());

        //TODO: Use service call: Don't need pledge information when calling the service. Can remove dataReconciler, probably.
        Map<String, Object> d = new HashMap<>(16);
        d.put("familyId", donor.getFamilyId());
        d.put("fundId", fundId);
        d.put("pledgeId", dataReconciler.getPledgeId(donor.getFamilyId(), fundId));
        d.put("amount", donation.getAmount());
        d.put("deductibleAmount", donation.isTaxDeductible() ? donation.getAmount(): 0.0);
        d.put("donationDate", donation.getDonationDate());
        d.put("donationType", "EGIFT");
        d.put("transactionId", donation.getTransactionId());

        LOG.debug("Creating donation for " + donor.getName());
        dataReconciler.createDonation(d);
        LOG.debug("Donation created!");
    }
}
