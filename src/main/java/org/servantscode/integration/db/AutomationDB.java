package org.servantscode.integration.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.Automation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class AutomationDB extends EasyDB<Automation> {
    private static final Logger LOG = LogManager.getLogger(AutomationDB.class);

    private static final Map<String, String> FIELD_MAP;

    static {
        FIELD_MAP = new HashMap<>();
        FIELD_MAP.put("nextSchedule", "next");
        FIELD_MAP.put("integrationName", "si.name");
    }

    public AutomationDB() {
        super(Automation.class, "si.name", FIELD_MAP);
    }

    private QueryBuilder query(QueryBuilder selection) {
        return selection.from("automations a")
                        .leftJoin("org_integrations oi ON oi.id=a.integration_id")
                        .leftJoin("system_integrations si ON si.id=oi.system_integration_id");
    }

    private QueryBuilder selectData() {
        return select("a.*", "si.name");
    }

    public int getCount(String search) {
        return getCount(query(count()).search(searchParser.parse(search)).inOrg("a.org_id"));
    }

    public Automation getAutomation(String automationName) {
        return getOne(query(selectData()).with("name", automationName).inOrg("a.org_id"));
    }

    public Automation getAutomation(int id) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query automations outside of org context");

        return getOne(query(selectData()).with("a.id", id).inOrg("a.org_id"));
    }

    public Automation getAutomationByKey(int integrationId, String key) {
        return getOne(query(selectData()).with("integration_id", integrationId).with("external_id", key));
    }

    public List<Automation> getAllReadyAutomations() {
        return get(query(selectData()).where("next < now() OR next IS NULL"));
    }

    public List<Automation> getAutomations(String search, String sortField, int start, int count) {
        if(OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query automations outside of org context");

        QueryBuilder query = query(selectData()).search(searchParser.parse(search)).inOrg("a.org_id")
                .page(sortField, start, count);
        return get(query);
    }

    public Automation create(Automation automation) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query automations outside of org context");
        return create(automation, OrganizationContext.orgId());
    }

    public Automation create(Automation automation, int orgId) {
        InsertBuilder cmd = insertInto("automations")
                .value("integration_id", automation.getIntegrationId())
                .value("cycle", automation.getCycle())
                .value("frequency", automation.getFrequency())
                .value("weekly_days", encodeDays(automation.getWeeklyDays()))
                .value("start", automation.getScheduleStart())
                .value("next", automation.getScheduleStart())
                .value("org_id", orgId);

        automation.setId(createAndReturnKey(cmd));
        return automation;
    }

    public Automation update(Automation automation) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot query automations outside of org context");
        return update(automation, OrganizationContext.orgId());
    }

    public Automation update(Automation automation, int org_id) {
        UpdateBuilder cmd = update("automations")
                .value("integration_id", automation.getIntegrationId())
                .value("cycle", automation.getCycle())
                .value("frequency", automation.getFrequency())
                .value("weekly_days", encodeDays(automation.getWeeklyDays()))
                .value("start", automation.getScheduleStart())
                .value("next", automation.getNextScheduled())
               .withId(automation.getId()).inOrg(org_id);
        if(!update(cmd))
            throw new RuntimeException("Could not update automation record");
        return automation;
    }

    public boolean deleteAutomation(int id) {
        if (OrganizationContext.getOrganization() == null)
            throw new RuntimeException("Cannot delete an automation outside of org context");
        return deleteAutomation(id, OrganizationContext.orgId());
    }

    public boolean deleteAutomation(int id, int orgId) {
        return delete(deleteFrom("automations").withId(id).inOrg(orgId));
    }

    // ----- Private -----
    @Override
    protected Automation processRow(ResultSet rs) throws SQLException {
        Automation a = new Automation();
        a.setId(rs.getInt("id"));
        a.setIntegrationId(rs.getInt("integration_id"));
        a.setIntegrationName(rs.getString("name"));
        a.setCycle(Automation.RecurrenceCycle.valueOf(rs.getString("cycle")));
        a.setFrequency(rs.getInt("frequency"));
        a.setWeeklyDays(decodeDays(rs.getInt("weekly_days")));
        a.setScheduleStart(convert(rs.getTimestamp("start")));
        a.setNextScheduled(convert(rs.getTimestamp("next")));
        a.setOrgId(rs.getInt("org_id"));
        return a;
    }

    private static int encodeDays(List<DayOfWeek> days) {
        int result = 0;
        if(days == null || days.isEmpty())
            return 0;

        for(DayOfWeek day: days)
            result += 1 << day.getValue()-1;

        return result;
    }

    private static List<DayOfWeek> decodeDays(int days) {
        if(days == 0)
            return emptyList();

        LinkedList<DayOfWeek> result = new LinkedList<>();
        for(int i=0; i<7; i++){
            if((days & 1 << i) != 0)
                result.add(DayOfWeek.of(i+1));
        }
        return result;
    }

}
