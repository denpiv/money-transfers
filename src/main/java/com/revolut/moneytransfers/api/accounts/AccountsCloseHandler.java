package com.revolut.moneytransfers.api.accounts;

import com.revolut.moneytransfers.config.ApplicationContext;
import com.revolut.moneytransfers.mapper.AccountMapper;
import com.revolut.moneytransfers.service.AccountService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class AccountsCloseHandler {

    private static final Logger LOGGER = LogManager.getLogger(AccountsCloseHandler.class);

    private static AccountService accountService = ApplicationContext.getInstance().getAccountService();
    private static AccountMapper accountMapper = ApplicationContext.getInstance().getAccountMapper();

    private AccountsCloseHandler() {
        throw new IllegalStateException();
    }

    public static void handle(RoutingContext routingContext) {
        AccountsCloseResponse accountsCloseResponse = new AccountsCloseResponse();
        HttpServerResponse response = routingContext.response();
        response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
        try {
            long accountId = Long.parseLong(routingContext.request().getParam("id"));
            if (accountId > 0) {
                accountsCloseResponse.setAccount(accountMapper.toDto(accountService.closeAccount(accountId)));
                response.setStatusCode(OK.code());
            } else {
                response.setStatusCode(BAD_REQUEST.code());
                accountsCloseResponse.setErrorMessage("Provided request field is invalid");
            }
        } catch (Exception e) {
            // TODO: review types of exceptions and response codes
            LOGGER.error(e.getMessage());
            accountsCloseResponse.setErrorMessage(e.getMessage());
            response.setStatusCode(INTERNAL_SERVER_ERROR.code());
        }
        response.end(Json.encodePrettily(accountsCloseResponse));
    }
}
