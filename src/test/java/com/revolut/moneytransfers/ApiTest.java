package com.revolut.moneytransfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.moneytransfers.api.accounts.AccountsCloseResponse;
import com.revolut.moneytransfers.api.accounts.AccountsCreateResponse;
import com.revolut.moneytransfers.api.accounts.AccountsGetAllResponse;
import com.revolut.moneytransfers.api.accounts.AccountsGetByIdResponse;
import com.revolut.moneytransfers.dto.AccountDto;
import com.revolut.moneytransfers.entity.Transaction;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.revolut.moneytransfers.HttpServerVerticle.ACCOUNTS_URL;
import static com.revolut.moneytransfers.HttpServerVerticle.ACCOUNTS_URL_BY_ID;
import static com.revolut.moneytransfers.HttpServerVerticle.TRANSFERS_URL;
import static com.revolut.moneytransfers.HttpServerVerticle.VERTICLE_PORT;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class ApiTest {

    private static final String LOCALHOST = "localhost";
    private static final String HEALTH_CHECK = "/health";
    private static ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    @DisplayName("Deploy a verticle")
    void prepare(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }


    @Test
    @DisplayName("Test API on different cases")
    void testAPI(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);

        // run health check
        testServiceHealthy(testContext, client);

        // create account 1
        String accountNumber1 = "123-456-78-1";
        String accountCurrency1 = "EUR";
        double initialBalance1 = 300;
        long userId1 = 1;
        String request1 = "{" +
                "\"number\":" + "\"" + accountNumber1 + "\"," +
                "\"currency\":" + "\"" + accountCurrency1 + "\"," +
                "\"balance\":" + initialBalance1 + "," +
                "\"userId\":" + userId1 +
                "}";
        JsonObject body1 = new JsonObject(request1);
        testCreateAccount(testContext, client, accountNumber1, accountCurrency1, initialBalance1, userId1, body1);
        // create account with the same account number should fail
        testCreateAccountWithExistingAccountNumber(testContext, client, body1);
        // create account 2
        String accountNumber2 = "123-456-78-2";
        String accountCurrency2 = "USD";
        double initialBalance2 = 500;
        long userId2 = 2;
        String request2 = "{" +
                "\"number\":" + "\"" + accountNumber2 + "\"," +
                "\"currency\":" + "\"" + accountCurrency2 + "\"," +
                "\"balance\":" + initialBalance2 + "," +
                "\"userId\":" + userId2 +
                "}";
        JsonObject body2 = new JsonObject(request2);
        testCreateAccount(testContext, client, accountNumber2, accountCurrency2, initialBalance2, userId2, body2);
        // test get all not closed accounts
        testGetAllAccounts(testContext, client);
        // test make successful transfer
        long accountIdFrom = 1;
        long accountIdTo = 3;
        double sum = 45.5;
        String request3 = "{" +
                "\"accountIdFrom\":" + accountIdFrom + "," +
                "\"accountIdTo\":" + accountIdTo + "," +
                "\"sum\":" + sum +
                "}";
        JsonObject body3 = new JsonObject(request3);
        testCreateTransfer(testContext, client, body3);
        // test get account with transactions by account id
        testGetAccountByIdWithTransactions(testContext, client, accountNumber1, accountCurrency1, initialBalance1, userId1, accountIdFrom, accountIdTo, sum);
        // test close account
        testCloseAccount(testContext, client, accountNumber1, accountCurrency1, initialBalance1, userId1, accountIdFrom, sum);
    }

    private void testCloseAccount(VertxTestContext testContext, WebClient client, String accountNumber1, String accountCurrency1, double initialBalance1, long userId1, long accountIdFrom, double sum) {
        client.delete(VERTICLE_PORT, LOCALHOST, ACCOUNTS_URL_BY_ID.replace(":id", String.valueOf(accountIdFrom)))
                .send(testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(OK.code(), response.statusCode());
                                    AccountsCloseResponse closeResponse = MAPPER.readValue(response.bodyAsString(), AccountsCloseResponse.class);
                                    AccountDto account = closeResponse.getAccount();
                                    assertAll("Response should match expected",
                                            () -> assertNull(closeResponse.getErrorMessage()),
                                            () -> assertEquals(accountNumber1, account.getNumber()),
                                            () -> assertEquals(accountCurrency1, account.getCurrency()),
                                            () -> assertEquals(initialBalance1 - sum, account.getBalance()),
                                            () -> assertEquals(userId1, account.getUserId()),
                                            () -> assertTrue(account.isClosed())
                                    );
                                    testContext.completeNow();
                                })
                        )
                );
    }

    private void testGetAccountByIdWithTransactions(VertxTestContext testContext, WebClient client, String accountNumber1, String accountCurrency1, double initialBalance1, long userId1, long accountIdFrom, long accountIdTo, double sum) {
        client.get(VERTICLE_PORT, LOCALHOST, ACCOUNTS_URL_BY_ID.replace(":id", String.valueOf(accountIdFrom)))
                .send(testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(OK.code(), response.statusCode());
                                    AccountsGetByIdResponse getResponse = MAPPER.readValue(response.bodyAsString(), AccountsGetByIdResponse.class);
                                    AccountDto account = getResponse.getAccount();
                                    List<Transaction> outgoingTransactions = account.getOutgoingTransactions();
                                    assertAll("Response should match expected",
                                            () -> assertNull(getResponse.getErrorMessage()),
                                            () -> assertEquals(accountNumber1, account.getNumber()),
                                            () -> assertEquals(accountCurrency1, account.getCurrency()),
                                            () -> assertEquals(initialBalance1 - sum, account.getBalance()),
                                            () -> assertEquals(userId1, account.getUserId()),
                                            () -> assertFalse(account.isClosed()),
                                            () -> assertEquals(1, outgoingTransactions.size()),
                                            () -> assertEquals(0, account.getIncomingTransactions().size()),
                                            () -> assertEquals(accountIdFrom, outgoingTransactions.get(0).getAccountIdFrom()),
                                            () -> assertEquals(accountIdTo, outgoingTransactions.get(0).getAccountIdTo()),
                                            () -> assertEquals(sum, outgoingTransactions.get(0).getSumFrom())
                                    );
                                })
                        )
                );
    }

    private void testCreateTransfer(VertxTestContext testContext, WebClient client, JsonObject body) {
        client.post(VERTICLE_PORT, LOCALHOST, TRANSFERS_URL)
                .sendJsonObject(body, testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(NO_CONTENT.code(), response.statusCode());
                                    assertNull(response.bodyAsString(), "Response should match expected");
                                })
                        )
                );
    }

    private void testGetAllAccounts(VertxTestContext testContext, WebClient client) {
        client.get(VERTICLE_PORT, LOCALHOST, ACCOUNTS_URL)
                .send(testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(OK.code(), response.statusCode());
                                    AccountsGetAllResponse getAllResponse = MAPPER.readValue(response.bodyAsString(), AccountsGetAllResponse.class);
                                    List<AccountDto> accounts = getAllResponse.getAccounts();
                                    assertAll("Response should match expected",
                                            () -> assertNull(getAllResponse.getErrorMessage()),
                                            () -> assertEquals(2, accounts.size())
                                    );
                                })
                        )
                );
    }

    private void testServiceHealthy(VertxTestContext testContext, WebClient client) {
        client.get(VERTICLE_PORT, LOCALHOST, HEALTH_CHECK)
                .send(testContext.succeeding(
                        response -> testContext.verify(
                                () -> assertEquals(NO_CONTENT.code(), response.statusCode()))
                        )
                );
    }

    private void testCreateAccountWithExistingAccountNumber(VertxTestContext testContext, WebClient client, JsonObject body) {
        client.post(VERTICLE_PORT, LOCALHOST, ACCOUNTS_URL)
                .sendJsonObject(body, testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(INTERNAL_SERVER_ERROR.code(), response.statusCode());
                                    AccountsCreateResponse createResponse = MAPPER.readValue(response.bodyAsString(), AccountsCreateResponse.class);
                                    assertAll("Response should match expected",
                                            () -> assertNull(createResponse.getAccount()),
                                            () -> assertNotNull(createResponse.getErrorMessage())
                                    );
                                })
                        )
                );
    }

    private void testCreateAccount(VertxTestContext testContext, WebClient client, String accountNumber, String accountCurrency, double initialBalance, long userId, JsonObject body) {
        client.post(VERTICLE_PORT, LOCALHOST, ACCOUNTS_URL)
                .sendJsonObject(body, testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(OK.code(), response.statusCode());
                                    AccountsCreateResponse createResponse = MAPPER.readValue(response.bodyAsString(), AccountsCreateResponse.class);
                                    AccountDto account = createResponse.getAccount();
                                    assertAll("Response should match expected",
                                            () -> assertNotNull(account),
                                            () -> assertEquals(accountNumber, account.getNumber()),
                                            () -> assertEquals(accountCurrency, account.getCurrency()),
                                            () -> assertEquals(initialBalance, account.getBalance()),
                                            () -> assertEquals(userId, account.getUserId()),
                                            () -> assertFalse(account.isClosed())
                                    );
                                })
                        )
                );
    }
}