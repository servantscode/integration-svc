package org.servantscode.integration.donation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.Organization;
import org.servantscode.integration.AsyncProcess;
import org.servantscode.integration.db.DataReconciler;
import org.servantscode.integration.pushpay.dao.PushPayPayment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordDonationProcess extends AsyncProcess implements Runnable {
    private static final Logger LOG = LogManager.getLogger(RecordDonationProcess.class);

    private List<PushPayPayment> payments;
    private Organization org;

    public RecordDonationProcess(List<PushPayPayment> payments, Organization org) {
        this.payments = payments;
        this.org = org;
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

        DataReconciler dataReconciler = new DataReconciler(org.getId());

        int fundId = dataReconciler.getFundIdForName(fundName);
        if(fundId <= 0) {
            LOG.debug("No fund found creating fund: " + fundName);
            fundId = dataReconciler.createFund(fundName);
        }

        int familyId = dataReconciler.getFamilyId(personName, personEmail, personPhone);
        if(familyId <= 0) {
            LOG.debug("No family found creating family: " + fundName);
            familyId = dataReconciler.createPerson(personName, personEmail, personPhone);
        }

        int pledgeId = dataReconciler.getPledgeId(familyId, fundId);

        Map<String, Object> d = new HashMap<>(16);
        d.put("familyId", familyId);
        d.put("fundId", fundId);
        d.put("pledgeId", pledgeId);
        d.put("amount", Double.parseDouble(payment.getAmount().getAmount()));
        d.put("deductibleAmount", payment.getFund().isTaxDeductible()? Double.parseDouble(payment.getAmount().getAmount()): 0.0);
        d.put("donationDate", payment.getCreatedOn());
        d.put("donationType", "EGIFT");
        d.put("transactionId", payment.getTransactionId());

        LOG.debug("Creating donation for " + personName);
        dataReconciler.createDonation(d);
        LOG.debug("Donation created!");
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
