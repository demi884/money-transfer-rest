package org.revoult.handler;

import com.google.gson.Gson;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * Created by Leo on 5/27/2018.
 */
public abstract class AbstractHttpHandler implements HttpHandler {

    protected void writeResponse(HttpServerExchange exchange, Object result, int code) {
        exchange.setStatusCode(code);
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(new Gson().toJson(result));

    }
}
