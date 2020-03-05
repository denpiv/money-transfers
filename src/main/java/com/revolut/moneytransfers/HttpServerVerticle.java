package com.revolut.moneytransfers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.revolut.moneytransfers.api.accounts.AccountsCloseHandler;
import com.revolut.moneytransfers.api.accounts.AccountsCreateHandler;
import com.revolut.moneytransfers.api.accounts.AccountsGetAllHandler;
import com.revolut.moneytransfers.api.accounts.AccountsGetByIdHandler;
import com.revolut.moneytransfers.api.transfers.TransfersCreateHandler;
import com.revolut.moneytransfers.config.ApplicationContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.sql.SQLException;

public class HttpServerVerticle extends AbstractVerticle {

    static final int VERTICLE_PORT = 8089;
    static final String HEALTH_CHECK_URL = "/health*";
    static final String ACCOUNTS_URL_BY_ID = "/accounts/:id";
    static final String ACCOUNTS_URL = "/accounts*";
    static final String TRANSFERS_URL = "/transfers*";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    /**
     * Method contains operations required to be performed when app starts.
     */
    @Override
    public void start() {
        ApplicationContext.init();
        try {
            ApplicationContext.getInstance().getDatabaseInitializer().init();
        } catch (SQLException e) {
            LOGGER.error("Database init failed: " + e.getMessage());
            return;
        }

        DatabindCodec.prettyMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        var router = Router.router(vertx);
        router.get(HEALTH_CHECK_URL).handler(HealthCheckHandler.create(vertx));
        router.get(ACCOUNTS_URL_BY_ID).handler(AccountsGetByIdHandler::handle);
        router.get(ACCOUNTS_URL).handler(AccountsGetAllHandler::handle);
        router.route(ACCOUNTS_URL).handler(BodyHandler.create());
        router.post(ACCOUNTS_URL).handler(AccountsCreateHandler::handle);
        router.delete(ACCOUNTS_URL_BY_ID).handler(AccountsCloseHandler::handle);
        router.route(TRANSFERS_URL).handler(BodyHandler.create());
        router.post(TRANSFERS_URL).handler(TransfersCreateHandler::handle);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(VERTICLE_PORT);
        LOGGER.info("HTTP Server Verticle: Started to listen requests on port " + VERTICLE_PORT);
    }
}
