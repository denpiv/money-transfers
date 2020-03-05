package com.revolut.moneytransfers.api.transfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.moneytransfers.config.ApplicationContext;
import com.revolut.moneytransfers.dto.TransferDto;
import com.revolut.moneytransfers.service.TransferService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

public class TransfersCreateHandler {

    private static final Logger LOGGER = LogManager.getLogger(TransfersCreateHandler.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static TransferService transferService = ApplicationContext.getInstance().getTransferService();

    private TransfersCreateHandler() {
        throw new IllegalStateException();
    }

    public static void handle(RoutingContext routingContext) {
        TransfersCreateResponse transfersCreateResponse = new TransfersCreateResponse();
        HttpServerResponse response = routingContext.response();
        response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
        try {
            TransferDto transferDto = MAPPER.readValue(routingContext.getBodyAsString(), TransferDto.class);
            transferService.transfer(transferDto);
            response.setStatusCode(NO_CONTENT.code());
        } catch (Exception e) {
            // TODO: review types of exceptions and response codes
            LOGGER.error(e.getMessage());
            transfersCreateResponse.setErrorMessage(e.getMessage());
            response.setStatusCode(INTERNAL_SERVER_ERROR.code());
        }
        response.end(Json.encodePrettily(transfersCreateResponse));
    }
}
