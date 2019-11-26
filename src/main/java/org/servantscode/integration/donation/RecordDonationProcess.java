package org.servantscode.integration.donation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.Organization;
import org.servantscode.integration.AsyncProcess;
import org.servantscode.integration.Donor;
import org.servantscode.integration.IncomingDonation;
import org.servantscode.integration.db.DataReconciler;
import org.servantscode.integration.db.DonorDB;
import org.servantscode.integration.db.IncomingDonationDB;
import org.servantscode.integration.pushpay.dao.PushPayPayment;

import java.util.List;

public class RecordDonationProcess extends AsyncProcess implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RecordDonationProcess.class);

    private List<PushPayPayment> payments;
    private Organization org;
    private int integrationId;

    private DonorDB donorDb;
    private IncomingDonationDB donationDb;
    private DonationRecorder recorder;

    public RecordDonationProcess(List<PushPayPayment> payments, Organization org, int integrationId) {
        this.payments = payments;
        this.org = org;
        this.integrationId = integrationId;

        this.donorDb = new DonorDB();
        this.donationDb = new IncomingDonationDB();

        this.recorder = new DonationRecorder(org.getId());
    }

    @Override
    public void run() {
        setState(AsyncStatus.RUNNING);
        try {
            for(PushPayPayment payment: payments) {
                storePayment(payment, org);
            }

            setState(AsyncStatus.COMPLETE);
        } catch(Throwable t) {
            LOG.error("Failed to sync egifts: ", t);
            setFailure(t);
        }
    }

    // ----- Private -----
    private void storePayment(PushPayPayment payment, Organization org) {
        String fundName = payment.getFund().getName();
        String personName = payment.getPayer().getFullName();
        String personEmail = payment.getPayer().getEmailAddress();
        String personPhone = formatPhoneNumber(payment.getPayer().getMobileNumber());
        String externalKey = payment.getPayer().getKey();

        DataReconciler dataReconciler = new DataReconciler(org.getId());

        if(dataReconciler.transactionExists(payment.getTransactionId()))
            return;

        //TODO: Call fund service to look up fund
        int fundId = dataReconciler.getFundIdForName(fundName);
        if(fundId <= 0) {
            LOG.debug("No fund found creating fund: " + fundName);
            fundId = dataReconciler.createFund(fundName);
        }

        Donor donor = donorDb.getDonorByKey(integrationId, externalKey);
        if(donor == null) {
            donor = new Donor();
            donor.setIntegrationId(integrationId);
            donor.setExternalId(externalKey);
            donor.setName(personName);
            donor.setEmail(personEmail);
            donor.setPhoneNumber(personPhone);
            donorDb.create(donor);
        }

        int familyId = donor.getFamilyId();
        if(familyId <= 0) {
            //TODO: Call service to look up family
            familyId = dataReconciler.getFamilyId(personName, personEmail, personPhone);
            donor.setFamilyId(familyId);
        }


        IncomingDonation donation = new IncomingDonation();
        donation.setIntegrationId(integrationId);
        donation.setExternalId(payment.getPaymentToken());
        donation.setFund(fundName);
        donation.setTransactionId(payment.getTransactionId());
        donation.setTaxDeductible(payment.getFund().isTaxDeductible());
        donation.setAmount(Float.parseFloat(payment.getAmount().getAmount()));
        donation.setDonationDate(payment.getCreatedOn());
        donation.setDonorId(donor.getId());

        if(familyId > 0) {
            recorder.record(donation);
        } else {
            donationDb.create(donation, org.getId());
        }
    }

    private static String formatPhoneNumber(String number) {
        if(number == null)
            return "";

        String[] digits = number.split("[^\\d]");
        String allDigits = String.join("", digits).trim();
        if(allDigits.length() == 7)
            return allDigits.substring(0,3) + "-" + allDigits.substring(3);
        else if(allDigits.length() == 10)
            return String.format("(%s) %s-%s", allDigits.substring(0,3), allDigits.substring(3, 7), allDigits.substring(7));
        else if(allDigits.length() == 11)
            return String.format("(%s) %s-%s", allDigits.substring(1,4), allDigits.substring(4, 8), allDigits.substring(8));
        else
            LOG.warn("Could not format phone number");
        return number;
    }
}
