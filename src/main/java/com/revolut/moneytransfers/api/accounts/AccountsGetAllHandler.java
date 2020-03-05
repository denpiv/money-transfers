package com.revolut.moneytransfers.api.accounts;

import com.revolut.moneytransfers.config.ApplicationContext;
import com.revolut.moneytransfers.mapper.AccountMapper;
import com.revolut.moneytransfers.service.AccountService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class AccountsGetAllHandler {

    public static final String SHOW_CLOSED_ACCOUNTS = "1";
    public static final String SHOW_CLOSED = "showClosed";
    private static final Logger LOGGER = LogManager.getLogger(AccountsGetAllHandler.class);
    private static AccountService accountService = ApplicationContext.getInstance().getAccountService();
    private static AccountMapper accountMapper = ApplicationContext.getInstance().getAccountMapper();

    private AccountsGetAllHandler() {
        throw new IllegalStateException();
    }

    public static void handle(RoutingContext routingContext) {
        AccountsGetAllResponse accountsGetAllResponse = new AccountsGetAllResponse();
        HttpServerResponse response = routingContext.response();
        response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
        try {
            String showClosed = routingContext.request().getParam(SHOW_CLOSED);
            accountsGetAllResponse.setAccounts(
                    accountService.findAccounts(SHOW_CLOSED_ACCOUNTS.equals(showClosed)).stream()
                            .map(accountMapper::toDto)
                            .collect(Collectors.toList())
            );
            response.setStatusCode(OK.code());
        } catch (Exception e) {
            // TODO: review types of exceptions and response codes
            LOGGER.error(e.getMessage());
            accountsGetAllResponse.setErrorMessage(e.getMessage());
            response.setStatusCode(INTERNAL_SERVER_ERROR.code());
        }
        response.end(Json.encodePrettily(accountsGetAllResponse));
    }
}
