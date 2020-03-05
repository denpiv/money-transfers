package com.revolut.moneytransfers.mapper;

import com.revolut.moneytransfers.dto.AccountDto;
import com.revolut.moneytransfers.entity.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {

    Account toEntity(AccountDto accountDto);

    AccountDto toDto(Account account);
}
