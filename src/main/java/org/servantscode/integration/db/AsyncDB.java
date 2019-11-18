package org.servantscode.integration.db;

import org.servantscode.commons.db.EasyDB;
import org.servantscode.integration.AsyncProcess;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AsyncDB extends EasyDB<AsyncProcess> {

    public AsyncDB() {
        super(AsyncProcess.class, "name");
    }
    @Override
    protected AsyncProcess processRow(ResultSet resultSet) throws SQLException {
        return null;
    }
}
