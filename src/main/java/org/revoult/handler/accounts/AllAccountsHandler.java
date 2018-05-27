package org.revoult.handler.accounts;

import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.revoult.dao.AccountDao;
import org.revoult.handler.AbstractHttpHandler;

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