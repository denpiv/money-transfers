package com.revolut.moneytransfers.service;

import com.j256.ormlite.dao.Dao;
import com.revolut.moneytransfers.dto.AccountDto;
import com.revolut.moneytransfers.entity.Account;
import com.revolut.moneytransfers.entity.Transaction;
import com.revolut.moneytransfers.mapper.AccountMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountService {

    private final Dao<Account, Long> accountRepository;
    private final Dao<Transaction, Long> transactionRepository;
    private final AccountMapper accountMapper;

    public AccountService(Dao<Account, Long> accountRepository,
                          Dao<Transaction, Long> transactionRepository,
                          AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountMapper = accountMapper;
    }

    public Account findAccount(long accountId) throws SQLException {
        return Optional.ofNullable(accountRepository.queryForId(accountId))
                .orElseThrow(() -> new NullPointerException("Account with id=" + accountId + " cannot be found"));
    }

    public AccountDto findAccountWithTransactions(long accountId) throws SQLException {
        Account account = findAccount(accountId);
        AccountDto accountDto = accountMapper.toDto(account);
        accountDto.setOutgoingTransactions(transactionRepository.queryForEq("accountIdFrom", accountId));
        accountDto.setIncomingTransactions(transactionRepository.queryForEq("accountIdTo", accountId));
        return accountDto;
    }

    public List<Account> findAccounts(boolean showClosed) throws SQLException {
        if (showClosed) {
            return accountRepository.queryForAll();
        }
        return accountRepository.queryForEq("closed", false);
    }

    public Account createAccount(Account account) throws SQLException {
        return accountRepository.createIfNotExists(account);
    }

    public Account closeAccount(long accountId) throws SQLException {
        Account account = findAccount(accountId);
        if (account.isClosed()) {
            throw new IllegalArgumentException("Account is already closed");
        }
        account.setClosed(true);
        accountRepository.update(account);
        return account;
    }

    public Account updateAccountBalance(Account account, double sum) throws SQLException {
        account.setBalance(account.getBalance() + sum);
        accountRepository.update(account);
        return account;
    }
}
