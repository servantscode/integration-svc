package org.servantscode.integration.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.ConfigUtils;
import org.servantscode.commons.db.AbstractDBUpgrade;

import java.sql.SQLException;

public class DBUpgrade extends AbstractDBUpgrade {
    private static final Logger LOG = LogManager.getLogger(DBUpgrade.class);

    @Override
    public void doUpgrade() throws SQLException {
        LOG.info("Verifying database structures.");

        if(!tableExists("system_integrations")) {
            LOG.info("-- Creating system_integrations table");
            runSql("CREATE TABLE system_integrations (id SERIAL PRIMARY KEY, " +
                                                     "name TEXT, " +
                                                     "config TEXT)");
        }

        if(!tableExists("org_integrations")) {
            LOG.info("-- Creating org_integrations table");
            runSql("CREATE TABLE org_integrations (id SERIAL PRIMARY KEY, " +
                                                  "system_integration_id INTEGER REFERENCES system_integrations(id) ON DELETE CASCADE, " +
                                                  "org_id INTEGER REFERENCES organizations(id) ON DELETE CASCADE, " +
                                                  "config TEXT)");
        }
    }
}
