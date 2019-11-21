package org.servantscode.integration.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.ConfigUtils;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.IncomingDonation;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class IncomingDonationDB extends EasyDB<IncomingDonation> {
    private static final Logger LOG = LogManager.getLogger(IncomingDonationDB.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public IncomingDonationDB() {
        super(IncomingDonation.class, "d.name");
    }

    private QueryBuilder query(QueryBuilder selection) {
        return selection.from("incoming_donations id")
                .leftJoin("donors d ON id.donor_id=d.external_id");
    }

    private QueryBuilder selectData() {
        return select("id.*", "d.name");
    }

    public int getCount(String search) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query incomingDonations outside of org context");

        return getCount(query(count()).search(searchParser.parse(search)).inOrg());
    }

    public IncomingDonation getIncomingDonation(String incomingDonationName) {
        return getOne(query(selectData()).with("name", incomingDonationName).inOrg());
    }

    public IncomingDonation getIncomingDonation(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query incomingDonations outside of org context");

        return getOne(query(selectData()).withId(id).inOrg());
    }

    public List<IncomingDonation> getIncomingDonations(String search, String sortField, int start, int count) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query incomingDonations outside of org context");

        QueryBuilder query = query(selectData()).search(searchParser.parse(search)).inOrg()
                .page(sortField, start, count);
        return get(query);
    }

    public IncomingDonation create(IncomingDonation incomingDonation) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query incomingDonations outside of org context");
        return create(incomingDonation, OrganizationContext.orgId());
    }

    public IncomingDonation create(IncomingDonation incomingDonation, int orgId) {
        InsertBuilder cmd = insertInto("incoming_donations")
                .value("integration_id", incomingDonation.getIntegrationId())
                .value("external_id", incomingDonation.getExternalId())
                .value("donor_id", incomingDonation.getDonorId())
                .value("fund", incomingDonation.getFund())
                .value("transaction_id", incomingDonation.getTransactionId())
                .value("amount", incomingDonation.getAmount())
                .value("tax_deductible", incomingDonation.isTaxDeductible())
                .value("donation_date", incomingDonation.getDonationDate())
                .value("org_id", orgId);

        incomingDonation.setId(createAndReturnKey(cmd));
        return incomingDonation;
    }

    public IncomingDonation update(IncomingDonation incomingDonation) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query incomingDonations outside of org context");
        return update(incomingDonation, OrganizationContext.orgId());
    }

    public IncomingDonation update(IncomingDonation incomingDonation, int org_id) {
        UpdateBuilder cmd = update("incoming_donations")
                .value("integration_id", incomingDonation.getIntegrationId())
                .value("external_id", incomingDonation.getExternalId())
                .value("donor_id", incomingDonation.getDonorId())
                .value("fund", incomingDonation.getFund())
                .value("transaction_id", incomingDonation.getTransactionId())
                .value("amount", incomingDonation.getAmount())
                .value("tax_deductible", incomingDonation.isTaxDeductible())
                .value("donation_date", incomingDonation.getDonationDate())
               .withId(incomingDonation.getId()).inOrg(org_id);
        if(!update(cmd))
            throw new RuntimeException("Could not update incomingDonation record");
        return incomingDonation;
    }

    public boolean deleteIncomingDonation(int id) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot delete an org incomingDonation outside of org context");
        return deleteIncomingDonation(id, OrganizationContext.orgId());
    }

    public boolean deleteIncomingDonation(int id, int orgId) {
        return delete(deleteFrom("incoming_donations").withId(id).inOrg(orgId));
    }

    // ----- Private -----
    @Override
    protected IncomingDonation processRow(ResultSet rs) throws SQLException {
        IncomingDonation i = new IncomingDonation();
        i.setId(rs.getInt("id"));
        i.setIntegrationId(rs.getInt("integration_id"));
        i.setExternalId(rs.getString("external_id"));
        i.setDonorId(rs.getString("donor_id"));
        i.setFund(rs.getString("fund"));
        i.setTransactionId(rs.getString("transaction_id"));
        i.setAmount(rs.getFloat("amount"));
        i.setTaxDeductible(rs.getBoolean("tax_deductible"));
        i.setDonationDate(convert(rs.getTimestamp("donation_date")));
        return i;
    }

    private static String toEncryptedString(Map<String, String> data) {
        try {
            String config = OBJECT_MAPPER.writeValueAsString(data);
            return ConfigUtils.encryptConfig(config);
        } catch (JsonProcessingException e) {
            LOG.error("Could not map configuration object to string for storage.");
            throw new RuntimeException("Could not map configuration object to string for storage.", e);
        }
    }

    private static Map<String, String> fromEncryptedString(String dataString) {
        try {
            String decryptedConfig = ConfigUtils.decryptConfig(dataString);
            return OBJECT_MAPPER.readValue(decryptedConfig, new TypeReference<Map<String, String>>(){});
        } catch (IOException e) {
            LOG.error("Could not read confgiuration object from storage.", e);
            throw new RuntimeException("Could not read confgiuration object from storage.", e);
        }
    }
}
