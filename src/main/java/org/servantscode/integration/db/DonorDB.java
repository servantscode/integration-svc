package org.servantscode.integration.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.Donor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DonorDB extends EasyDB<Donor> {
    private static final Logger LOG = LogManager.getLogger(DonorDB.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public DonorDB() {
        super(Donor.class, "name");
    }

    private QueryBuilder query(QueryBuilder selection) {
        return selection.from("donors");
    }

    private QueryBuilder selectData() {
        return selectAll();
    }

    public int getCount(String search) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query donors outside of org context");

        return getCount(query(count()).search(searchParser.parse(search)).inOrg());
    }

    public Donor getDonor(String donorName) {
        return getOne(query(selectData()).with("name", donorName).inOrg());
    }

    public Donor getDonor(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query donors outside of org context");

        return getOne(query(selectData()).withId(id).inOrg());
    }


    public Donor getDonorByKey(int integrationId, String key) {
        return getOne(query(selectData()).with("integration_id", integrationId).with("external_id", key));
    }

    public List<Donor> getDonors(String search, String sortField, int start, int count) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org donors outside of org context");

        QueryBuilder query = query(selectData()).search(searchParser.parse(search)).inOrg()
                .page(sortField, start, count);
        return get(query);
    }

    public Donor create(Donor donor) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org donors outside of org context");
        return create(donor, OrganizationContext.orgId());
    }

    public Donor create(Donor donor, int orgId) {
        InsertBuilder cmd = insertInto("donors")
                .value("integration_id", donor.getIntegrationId())
                .value("external_id", donor.getExternalId())
                .value("person_id", donor.getPersonId())
                .value("family_id", donor.getFamilyId())
                .value("name", donor.getName())
                .value("email", donor.getEmail())
                .value("phone_number", donor.getPhoneNumber())
                .value("org_id", orgId);

        donor.setId(createAndReturnKey(cmd));
        return donor;
    }

    public Donor update(Donor donor) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org donors outside of org context");
        return update(donor, OrganizationContext.orgId());
    }

    public Donor update(Donor donor, int org_id) {
        UpdateBuilder cmd = update("donors")
                .value("integration_id", donor.getIntegrationId())
                .value("external_id", donor.getExternalId())
                .value("person_id", donor.getPersonId())
                .value("family_id", donor.getFamilyId())
                .value("name", donor.getName())
                .value("email", donor.getEmail())
                .value("phone_number", donor.getPhoneNumber())
                .withId(donor.getId()).inOrg(org_id);
        if(!update(cmd))
            throw new RuntimeException("Could not update donor record");
        return donor;
    }

    public boolean deleteDonor(int id) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot delete an org donor outside of org context");
        return deleteDonor(id, OrganizationContext.orgId());
    }

    public boolean deleteDonor(int id, int orgId) {
        return delete(deleteFrom("donors").withId(id).inOrg(orgId));
    }

    // ----- Private -----
    @Override
    protected Donor processRow(ResultSet rs) throws SQLException {
        Donor i = new Donor();
        i.setId(rs.getInt("id"));
        i.setIntegrationId(rs.getInt("integration_id"));
        i.setExternalId(rs.getString("external_id"));
        i.setPersonId(rs.getInt("person_id"));
        i.setFamilyId(rs.getInt("family_id"));
        i.setName(rs.getString("name"));
        i.setEmail(rs.getString("email"));
        i.setPhoneNumber(rs.getString("phone_number"));
        return i;
    }
}
