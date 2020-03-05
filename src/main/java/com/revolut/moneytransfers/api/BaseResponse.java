package com.revolut.moneytransfers.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseResponse {
    private String errorMessage;
}
