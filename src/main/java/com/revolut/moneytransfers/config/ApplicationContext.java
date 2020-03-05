package com.revolut.moneytransfers.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.moneytransfers.mapper.AccountMapper;
import com.revolut.moneytransfers.mapper.AccountMapperImpl;
import com.revolut.moneytransfers.service.AccountService;
import com.revolut.moneytransfers.service.TransferService;

public class ApplicationContext {

    private static ApplicationContext instance;
    private final AccountService accountService;
    private final TransferService transferService;
    private final AccountMapper accountMapper;
    private final DatabaseInitializer databaseInitializer;

    private ApplicationContext() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        Injector injector = Guice.createInjector(new ApplicationModule());
        accountService = injector.getInstance(AccountService.class);
        accountMapper = injector.getInstance(AccountMapperImpl.class);
        transferService = injector.getInstance(TransferService.class);
        databaseInitializer = injector.getInstance(DatabaseInitializer.class);
    }

    public static void init() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public AccountMapper getAccountMapper() {
        return accountMapper;
    }

    public TransferService getTransferService() {
        return transferService;
    }

    public DatabaseInitializer getDatabaseInitializer() {
        return databaseInitializer;
    }
}
