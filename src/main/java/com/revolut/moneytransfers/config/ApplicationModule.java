package com.revolut.moneytransfers.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.revolut.moneytransfers.entity.Account;
import com.revolut.moneytransfers.entity.Transaction;
import com.revolut.moneytransfers.mapper.AccountMapper;
import com.revolut.moneytransfers.service.AccountService;
import com.revolut.moneytransfers.service.TransferService;
import org.mapstruct.factory.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;

public class ApplicationModule extends AbstractModule {

    @Inject
    @Singleton
    @Provides
    public JdbcPooledConnectionSource connectionSource() throws SQLException {
        return new JdbcPooledConnectionSource("jdbc:h2:mem:moneyTransfers");
    }

    @Inject
    @Singleton
    @Provides
    public AccountMapper accountMapper() {
        return Mappers.getMapper(AccountMapper.class);
    }

    @Inject
    @Singleton
    @Provides
    public Dao<Account, Long> accountRepository(final JdbcPooledConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, Account.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    @Singleton
    @Provides
    public Dao<Transaction, Long> transactionRepository(final JdbcPooledConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, Transaction.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    @Singleton
    @Provides
    public AccountService accountService(final Dao<Account, Long> accountRepository,
                                         final Dao<Transaction, Long> transactionRepository,
                                         final AccountMapper accountMapper) {
        return new AccountService(accountRepository, transactionRepository, accountMapper);
    }

    @Inject
    @Singleton
    @Provides
    public TransferService transferService(final Dao<Transaction, Long> transactionRepository,
                                           final AccountService accountService,
                                           final JdbcPooledConnectionSource connectionSource) {
        return new TransferService(transactionRepository, accountService, connectionSource);
    }
}