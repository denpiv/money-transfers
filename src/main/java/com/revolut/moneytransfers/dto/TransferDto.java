package com.revolut.moneytransfers.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TransferDto {
    Long id;
    Long accountIdFrom;
    Long accountIdTo;
    double sum;
}
