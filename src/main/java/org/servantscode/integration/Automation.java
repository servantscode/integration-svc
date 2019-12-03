package org.servantscode.integration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public class Automation {
    public enum RecurrenceCycle {HOURLY, DAILY, WEEKLY, DAY_OF_MONTH, WEEKDAY_OF_MONTH, YEARLY};

    private int id;
    private int integrationId;
    private String integrationName;

    private RecurrenceCycle cycle;
    private int frequency;
    private List<DayOfWeek> weeklyDays;
    private ZonedDateTime scheduleStart;

    private ZonedDateTime nextScheduled;

    @JsonIgnore
    private int orgId;

    // ----- Accessors ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIntegrationId() { return integrationId; }
    public void setIntegrationId(int integrationId) { this.integrationId = integrationId; }

    public String getIntegrationName() { return integrationName; }
    public void setIntegrationName(String integrationName) { this.integrationName = integrationName; }

    public RecurrenceCycle getCycle() { return cycle; }
    public void setCycle(RecurrenceCycle cycle) { this.cycle = cycle; }

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }

    public List<DayOfWeek> getWeeklyDays() { return weeklyDays; }
    public void setWeeklyDays(List<DayOfWeek> weeklyDays) { this.weeklyDays = weeklyDays; }

    public ZonedDateTime getScheduleStart() { return scheduleStart; }
    public void setScheduleStart(ZonedDateTime scheduleStart) { this.scheduleStart = scheduleStart; }

    public ZonedDateTime getNextScheduled() { return nextScheduled; }
    public void setNextScheduled(ZonedDateTime nextScheduled) { this.nextScheduled = nextScheduled; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
}
