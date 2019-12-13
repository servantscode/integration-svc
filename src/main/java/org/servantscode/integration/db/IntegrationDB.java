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
import org.servantscode.integration.Integration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class IntegrationDB extends EasyDB<Integration> {
    private static final Logger LOG = LogManager.getLogger(IntegrationDB.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public IntegrationDB() {
        super(Integration.class, "name");
    }

    private QueryBuilder query(QueryBuilder selection) {
        return selection.from("org_integrations oi")
                .leftJoin("system_integrations si ON oi.system_integration_id = si.id")
                .leftJoin("organizations o ON oi.org_id=o.id")
                .leftJoin("automations a ON a.integration_id = oi.id");
    }

    private QueryBuilder selectData() {
        return select("oi.*", "si.name", "a.next", "a.id AS automation_id");
    }

    public int getCount(String search) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org integrations outside of org context");

        return getCount(query(count()).search(searchParser.parse(search)).inOrg("oi.org_id"));
    }

    public Integration getIntegration(String integrationName, String orgPrefix) {
        return getOne(query(selectData()).with("si.name", integrationName).with("host_name", orgPrefix));
    }

    public Integration getIntegration(String integrationName, int orgId) {
        return getOne(query(selectData()).with("si.name", integrationName).with("oi.org_id", orgId));
    }

    public Integration getIntegration(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org integrations outside of org context");

        return getOne(query(selectData()).with("oi.id", id).inOrg("oi.org_id"));
    }

    public List<Integration> getIntegrations(String search, String sortField, int start, int count) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query org integrations outside of org context");

        QueryBuilder query = query(selectData()).search(searchParser.parse(search)).inOrg("oi.org_id")
                .page(sortField, start, count);
        return get(query);
    }

    public Integration create(Integration integration) {
        InsertBuilder cmd = insertInto("org_integrations")
                .value("system_integration_id", integration.getSystemIntegrationId())
                .value("config", toEncryptedString(integration.getConfig()))
                .value("failure", integration.getFailure())
                .value("last_sync", integration.getLastSync())
                .value("org_id", integration.getOrgId());
        if(!create(cmd))
            throw new RuntimeException("Could not create integration record");
        return integration;
    }

    public Integration update(Integration integration) {
        UpdateBuilder cmd = update("org_integrations")
                .value("config", toEncryptedString(integration.getConfig()))
                .value("failure", integration.getFailure())
                .value("last_sync", integration.getLastSync())
                .withId(integration.getId())
                .with("org_id", integration.getOrgId());
        if(!update(cmd))
            throw new RuntimeException("Could not update integration record");
        return integration;
    }

    public boolean deleteIntegration(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot delete an org integration outside of org context");
        return delete(deleteFrom("org_integrations").withId(id).inOrg("org_id"));
    }

    // ----- Private -----
    @Override
    protected Integration processRow(ResultSet rs) throws SQLException {
        Integration i = new Integration();
        i.setId(rs.getInt("id"));
        i.setSystemIntegrationId(rs.getInt("system_integration_id"));
        i.setAutomationId(rs.getInt("automation_id"));
        i.setName(rs.getString("name"));
        i.setFailure(rs.getString("failure"));
        i.setLastSync(convert(rs.getTimestamp("last_sync")));
        i.setNextScheduledSync(convert(rs.getTimestamp("next")));
        i.setConfig(fromEncryptedString(rs.getString("config")));
        i.setOrgId(rs.getInt("org_id"));
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
