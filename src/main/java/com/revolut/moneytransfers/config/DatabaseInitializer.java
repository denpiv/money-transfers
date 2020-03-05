package com.revolut.moneytransfers.config;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.revolut.moneytransfers.entity.Account;
import com.revolut.moneytransfers.entity.Transaction;

import javax.inject.Inject;
import java.sql.SQLException;

public class DatabaseInitializer {

    private final JdbcPooledConnectionSource connectionSource;

    @Inject
    public DatabaseInitializer(JdbcPooledConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    public void init() throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, Account.class);
        TableUtils.createTableIfNotExists(connectionSource, Transaction.class);
    }
}
