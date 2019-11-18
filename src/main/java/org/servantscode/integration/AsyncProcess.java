package org.servantscode.integration;

import org.servantscode.integration.db.AsyncDB;
import org.servantscode.integration.donation.RecordDonationProcess;

public abstract class AsyncProcess {
    public enum AsyncStatus { CREATED, RUNNING, FAILED, COMPLETE };

    private AsyncStatus state;
    private String failureMessage;

    private static AsyncDB db;

    public AsyncProcess() {
        this.db = new AsyncDB();
    }

    protected void setState(AsyncStatus state) {
        this.state = state;
    }

    protected void setFailure(Throwable t) {
        this.state = AsyncStatus.FAILED;
        this.failureMessage = t.getMessage();
    }
}
