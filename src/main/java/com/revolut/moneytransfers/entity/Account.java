package com.revolut.moneytransfers.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "accounts")
public class Account {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, unique = true)
    private String number;

    @DatabaseField(canBeNull = false)
    private String currency;

    @DatabaseField
    private double balance;

    @DatabaseField
    private boolean closed;

    @DatabaseField(canBeNull = false)
    private long userId;
}
