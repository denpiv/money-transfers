package com.revolut.moneytransfers.api.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.moneytransfers.config.ApplicationContext;
import com.revolut.moneytransfers.dto.AccountDto;
import com.revolut.moneytransfers.mapper.AccountMapper;
import com.revolut.moneytransfers.service.AccountService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class AccountsCreateHandler {

    private static final Logger LOGGER = LogManager.getLogger(AccountsCreateHandler.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static AccountService accountService = ApplicationContext.getInstance().getAccountService();
    private static AccountMapper accountMapper = ApplicationContext.getInstance().getAccountMapper();

    private AccountsCreateHandler() {
        throw new IllegalStateException();
    }

    public static void handle(RoutingContext routingContext) {
        AccountsCreateResponse accountsCreateResponse = new AccountsCreateResponse();
        HttpServerResponse response = routingContext.response();
        response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
        try {
            AccountDto accountDto = MAPPER.readValue(routingContext.getBodyAsString(), AccountDto.class);
            accountsCreateResponse.setAccount(
                    accountMapper.toDto(accountService.createAccount(accountMapper.toEntity(accountDto)))
            );
            response.setStatusCode(OK.code());
        } catch (Exception e) {
            // TODO: review types of exceptions and response codes
            LOGGER.error(e.getMessage());
            accountsCreateResponse.setErrorMessage(e.getMessage());
            response.setStatusCode(INTERNAL_SERVER_ERROR.code());
        }
        response.end(Json.encodePrettily(accountsCreateResponse));
    }
}
