package org.revolut.handler.accounts;

import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.revolut.dao.AccountDao;
import org.revolut.handler.AbstractHttpHandler;

/**
 * Created by Leo on 5/27/2018.
 */
public class AllAccountsHandler extends AbstractHttpHandler {
    @Inject
    private AccountDao accountDao;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        writeResponse(exchange, accountDao.fetchAll(), StatusCodes.OK);
    }
}