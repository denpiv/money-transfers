package com.revolut.moneytransfers.dto;

import com.revolut.moneytransfers.entity.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AccountDto {
    Long id;
    String number;
    String currency;
    double balance;
    boolean closed;
    Long userId;
    List<Transaction> incomingTransactions;
    List<Transaction> outgoingTransactions;
}
