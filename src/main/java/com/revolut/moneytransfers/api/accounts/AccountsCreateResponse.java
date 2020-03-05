package com.revolut.moneytransfers.api.accounts;

import com.revolut.moneytransfers.api.BaseResponse;
import com.revolut.moneytransfers.dto.AccountDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountsCreateResponse extends BaseResponse {
    private AccountDto account;
}
