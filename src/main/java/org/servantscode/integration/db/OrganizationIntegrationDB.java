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
import org.servantscode.integration.OrganizationIntegration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrganizationIntegrationDB extends EasyDB<OrganizationIntegration> {
    private static final Logger LOG = LogManager.getLogger(OrganizationIntegrationDB.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public OrganizationIntegrationDB() {
        super(OrganizationIntegration.class, "name");
    }

    private QueryBuilder query(QueryBuilder selection) {
        return selection.from("org_integrations oi")
                .leftJoin("system_integrations si ON oi.system_integration_id = si.id")
                .leftJoin("organizations o ON oi.org_id=o.id");
    }

    private QueryBuilder selectData() {
        return select("oi.*", "si.name");
    }

    public int getCount(String search) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org integrations outside of org context");

        return getCount(query(count()).search(searchParser.parse(search)).inOrg());
    }

    public OrganizationIntegration getOrganizationIntegration(String integrationName, String orgPrefix) {
        return getOne(query(selectData()).with("si.name", integrationName).with("host_name", orgPrefix));
    }

    public OrganizationIntegration getOrganizationIntegration(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org integrations outside of org context");

        return getOne(query(selectData()).with("oi.id", id).inOrg());
    }

    public List<OrganizationIntegration> getOrganizationIntegrations(String search, String sortField, int start, int count) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org integrations outside of org context");

        QueryBuilder query = query(selectData()).search(searchParser.parse(search)).inOrg()
                .page(sortField, start, count);
        return get(query);
    }

    public OrganizationIntegration create(OrganizationIntegration organizationIntegration) {
        InsertBuilder cmd = insertInto("org_integrations")
                .value("system_integration_id", organizationIntegration.getSystemIntegrationId())
                .value("config", toEncryptedString(organizationIntegration.getConfig()))
                .value("org_id", organizationIntegration.getOrgId());
        if(!create(cmd))
            throw new RuntimeException("Could not create organizationIntegration record");
        return organizationIntegration;
    }

    public OrganizationIntegration updateOrganizationIntegration(OrganizationIntegration organizationIntegration) {
        UpdateBuilder cmd = update("org_integrations")
                .value("config", toEncryptedString(organizationIntegration.getConfig()))
                .withId(organizationIntegration.getId())
                .with("org_id", organizationIntegration.getOrgId());
        if(!update(cmd))
            throw new RuntimeException("Could not update organizationIntegration record");
        return organizationIntegration;
    }

    public boolean deleteOrganizationIntegration(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot delete an org integration outside of org context");
        return delete(deleteFrom("org_integrations").withId(id).inOrg());
    }

    // ----- Private -----
    @Override
    protected OrganizationIntegration processRow(ResultSet rs) throws SQLException {
        OrganizationIntegration organizationIntegration = new OrganizationIntegration();
        organizationIntegration.setId(rs.getInt("id"));
        organizationIntegration.setSystemIntegrationId(rs.getInt("system_integration_id"));
        organizationIntegration.setName(rs.getString("name"));
        organizationIntegration.setConfig(fromEncryptedString(rs.getString("config")));
        organizationIntegration.setOrgId(rs.getInt("org_id"));
        return organizationIntegration;
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
