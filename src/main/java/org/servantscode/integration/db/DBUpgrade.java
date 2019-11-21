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

        if(!tableExists("incoming_donations")) {
            LOG.info("-- Creating incoming_donations table");
            runSql("CREATE TABLE incoming_donations (integration_id INTEGER REFERENCES org_integrations(id) ON DELETE CASCADE, " +
                                                    "external_id TEXT, " +
                                                    "transaction_id TEXT, " +
                                                    "fund TEXT, " +
                                                    "donor_id TEXT, " +
                                                    "amount float, " +
                                                    "donation_date TIMESTAMP WITH TIME ZONE, " +
                                                    "tax_deductible BOOLEAN, " +
                                                    "org_id INTEGER REFERENCES organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("external_donors")) {
            LOG.info("-- Creating external_donors table");
            runSql("CREATE TABLE external_donors (integration_id INTEGER REFERENCES org_integrations(id) ON DELETE CASCADE, " +
                                                 "external_id TEXT, " +
                                                 "internal_id INTEGER, " +
                                                 "family_id INTEGER, " +
                                                 "name TEXT, " +
                                                 "email TEXT, " +
                                                 "phone_number TEXT, " +
                                                 "org_id INTEGER REFERENCES organizations(id) ON DELETE CASCADE)");
        }

        ensureColumn("org_integrations", "failure", "TEXT");
        ensureColumn("org_integrations", "last_sync", "TIMESTAMP WITH TIME ZONE");
    }
}
