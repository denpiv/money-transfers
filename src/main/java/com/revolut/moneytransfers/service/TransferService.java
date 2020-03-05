package com.revolut.moneytransfers.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.revolut.moneytransfers.dto.TransferDto;
import com.revolut.moneytransfers.entity.Account;
import com.revolut.moneytransfers.entity.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;


public class TransferService {

    private final Dao<Transaction, Long> transactionRepository;
    private final AccountService accountService;
    private final JdbcPooledConnectionSource connectionSource;

    Map<String, Double> conversionRates = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("USD->USD", 1.0),
            new AbstractMap.SimpleEntry<>("USD->EUR", 0.9),
            new AbstractMap.SimpleEntry<>("USD->GBP", 0.78),
            new AbstractMap.SimpleEntry<>("EUR->EUR", 1.0),
            new AbstractMap.SimpleEntry<>("EUR->USD", 1.11),
            new AbstractMap.SimpleEntry<>("EUR->GBP", 0.87),
            new AbstractMap.SimpleEntry<>("GBP->GBP", 1.0),
            new AbstractMap.SimpleEntry<>("GBP->EUR", 1.16),
            new AbstractMap.SimpleEntry<>("GBP->USD", 1.29)
    );

    public TransferService(Dao<Transaction, Long> transactionRepository,
                           AccountService accountService,
                           JdbcPooledConnectionSource connectionSource) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.connectionSource = connectionSource;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void transfer(TransferDto transferDto) throws SQLException {
        Account accountFrom = accountService.findAccount(transferDto.getAccountIdFrom());
        validateAccountFrom(transferDto.getSum(), accountFrom);
        Account accountTo = accountService.findAccount(transferDto.getAccountIdTo());
        validateAccountTo(accountTo);
        double sumTo = calculateSumTo(transferDto, accountFrom.getCurrency(), accountTo.getCurrency());
        int transferResult = TransactionManager.callInTransaction(connectionSource,
                () -> {
                    Transaction transaction = createTransaction(
                            transferDto, accountFrom.getCurrency(), accountTo.getCurrency(), sumTo);
                    if (transaction.getId() != null) {
                        accountService.updateAccountBalance(accountFrom, -transaction.getSumFrom());
                        accountService.updateAccountBalance(accountTo, transaction.getSumTo());
                    }
                    return 1;
                });
        if (transferResult != 1) {
            throw new IllegalArgumentException("Transaction failed to complete");
        }
    }

    private void validateAccountFrom(double transferSum, Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be found");
        }
        if (account.isClosed()) {
            throw new IllegalArgumentException("Source account is closed");
        }
        if (transferSum <= 0) {
            throw new IllegalArgumentException("Sum of transfer should be > 0");
        }
        if (account.getBalance() < transferSum) {
            throw new IllegalArgumentException("Not enough funds to make transfer");
        }

    }

    private void validateAccountTo(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be found");
        }
        if (account.isClosed()) {
            throw new IllegalArgumentException("Target account is closed");
        }
    }

    private double calculateSumTo(TransferDto transferDto, String currencyFrom, String currencyTo) {
        String operation = currencyFrom.concat("->").concat(currencyTo);
        double rate = Optional.ofNullable(conversionRates.get(operation))
                .orElseThrow(() -> new IllegalArgumentException(operation + " conversion rate is not set"));
        return round(transferDto.getSum() * rate, 2);
    }

    private Transaction createTransaction(TransferDto transferDto, String currencyFrom, String currencyTo, double sumTo)
            throws SQLException {
        Transaction transaction = Transaction.builder()
                .accountIdFrom(transferDto.getAccountIdFrom())
                .currencyFrom(currencyFrom)
                .sumFrom(transferDto.getSum())
                .accountIdTo(transferDto.getAccountIdTo())
                .currencyTo(currencyTo)
                .sumTo(sumTo)
                .build();
        transactionRepository.create(transaction);
        return transaction;
    }
}
