package com.revolut.moneytransfers.api.accounts;

import com.revolut.moneytransfers.api.BaseResponse;
import com.revolut.moneytransfers.dto.AccountDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountsGetAllResponse extends BaseResponse {
    private List<AccountDto> accounts;
}
