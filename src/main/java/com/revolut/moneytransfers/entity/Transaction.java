package com.revolut.moneytransfers.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DatabaseTable(tableName = "transactions")
public class Transaction {

    @DatabaseField(generatedId = true)
    Long id;

    @DatabaseField(canBeNull = false)
    Long accountIdFrom;

    @DatabaseField(canBeNull = false)
    String currencyFrom;

    @DatabaseField(canBeNull = false)
    Long accountIdTo;

    @DatabaseField(canBeNull = false)
    String currencyTo;

    @DatabaseField
    double sumFrom;

    @DatabaseField
    double sumTo;
}
