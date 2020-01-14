package org.servantscode.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.integration.db.AutomationDB;
import org.servantscode.integration.pushpay.PushPaySynchronizer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class Automator extends TimerTask implements ServletContextListener {
    private static final Logger LOG = LogManager.getLogger(Automator.class);

    private final AutomationDB db;
    private final Timer timer;

    private static final long POLL_PERIOD = 60*60*1000; //Check every hour

    public Automator() {
        this.db = new AutomationDB();
        this.timer = new Timer();
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOG.debug("Starting Automator.");
        timer.scheduleAtFixedRate(this, 0, POLL_PERIOD);
    }

    //TODO: Genericize the synchronizing
    @Override
    public void run() {
        LOG.debug("Syncing ready automated integrations.");
        List<Automation> ready = db.getAllReadyAutomations();
        if (ready.isEmpty())
            return;

        LOG.info("Syncing " + ready.size() + " ready automations");

        for (Automation automaton : ready) {
            try {
                PushPaySynchronizer sync = new PushPaySynchronizer();
                sync.synchronize(automaton.getOrgId());

                automaton.setNextScheduled(computeNext(automaton));
                db.update(automaton, automaton.getOrgId());
            } catch (Throwable t) {
                LOG.error("Syncing " + automaton.getIntegrationName() + " failed for organization: " + automaton.getOrgId(), t);
            }
        }
        LOG.debug("Syncing integrations complete.");
    }

    private ZonedDateTime computeNext(Automation automaton) {
        ZonedDateTime next = automaton.getNextScheduled();
        if(next == null)
            next = ZonedDateTime.now();

        switch (automaton.getCycle()) {
            case HOURLY:
                next = next.plusHours(automaton.getFrequency());
                break;
            case DAILY:
                next = next.plusDays(automaton.getFrequency());
                break;
            case WEEKLY:
                List<DayOfWeek> days = automaton.getWeeklyDays();
                days.sort(Comparator.comparingInt(DayOfWeek::getValue));
                Iterator<DayOfWeek> dayIter = getDayOfWeekIterator(next, days);

                if(!dayIter.hasNext()) {
                    next = next.plusWeeks(automaton.getFrequency() - 1);
                    dayIter = days.iterator();;
                }

                next = next.with(TemporalAdjusters.next(dayIter.next()));
                break;
            case DAY_OF_MONTH:
                next = next.plusMonths(automaton.getFrequency());
                break;
            case WEEKDAY_OF_MONTH:
                DayOfWeek dayOfWeek = next.getDayOfWeek();
                int weekInMonth = ((next.getDayOfMonth()-1)/7) + 1;
                next = next.plusMonths(automaton.getFrequency()).with(TemporalAdjusters.dayOfWeekInMonth(weekInMonth, dayOfWeek));
                break;
            case YEARLY:
                next = next.plusYears(automaton.getFrequency());
                break;
        };

        return next;
    }

    private Iterator<DayOfWeek> getDayOfWeekIterator(ZonedDateTime next, List<DayOfWeek> days) {
        DayOfWeek today = ZonedDateTime.now().getDayOfWeek();

        Iterator<DayOfWeek> dayIter = days.iterator();
        for(DayOfWeek day: days) {
            if(day.getValue() <= today.getValue())
                dayIter.next();
        }
        return dayIter;
    }
}
