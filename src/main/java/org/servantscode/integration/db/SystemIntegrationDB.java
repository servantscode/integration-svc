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
import org.servantscode.integration.AvailableIntegration;
import org.servantscode.integration.SystemIntegration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SystemIntegrationDB extends EasyDB<SystemIntegration> {
    private static final Logger LOG = LogManager.getLogger(SystemIntegrationDB.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SystemIntegrationDB() {
        super(SystemIntegration.class, "name");
    }

    private QueryBuilder query(QueryBuilder selection) {
        return selection.from("system_integrations");
    }

    public int getCount(String search) {
        return getCount(query(count()).search(searchParser.parse(search)));
    }

    public SystemIntegration getSystemIntegration(int id) {
        return getOne(query(selectAll()).withId(id));
    }

    public SystemIntegration getSystemIntegrationByName(String name) {
        return getOne(query(selectAll()).with("name", name));
    }

    public List<SystemIntegration> getSystemIntegrations(String search, String sortField, int start, int count) {
        QueryBuilder query = query(selectAll()).search(searchParser.parse(search))
                .page(sortField, start, count);
        return get(query);
    }

    public SystemIntegration create(SystemIntegration systemIntegration) {
        InsertBuilder cmd = insertInto("system_integrations")
                .value("name", systemIntegration.getName())
                .value("config", toEncryptedString(systemIntegration.getConfig()));
        if(!create(cmd))
            throw new RuntimeException("Could not create systemIntegration record");
        return systemIntegration;
    }

    public SystemIntegration updateSystemIntegration(SystemIntegration systemIntegration) {
        UpdateBuilder cmd = update("system_integrations")
                .value("name", systemIntegration.getName())
                .value("config", toEncryptedString(systemIntegration.getConfig()))
                .withId(systemIntegration.getId());
        if(!update(cmd))
            throw new RuntimeException("Could not update systemIntegration record");
        return systemIntegration;
    }

    public boolean deleteSystemIntegration(int id) {
        return delete(deleteFrom("system_integrations").withId(id));
    }

    // ----- Private -----
    @Override
    protected SystemIntegration processRow(ResultSet rs) throws SQLException {
        SystemIntegration systemIntegration = new SystemIntegration();
        systemIntegration.setId(rs.getInt("id"));
        systemIntegration.setName(rs.getString("name"));
        systemIntegration.setConfig(fromEncryptedString(rs.getString("config")));
        return systemIntegration;
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
