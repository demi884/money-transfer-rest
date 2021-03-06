package org.revolut.handler.transactions;

import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.revolut.dao.TransactionDao;
import org.revolut.handler.AbstractHttpHandler;

/**
 * Created by Leo on 5/27/2018.
 */
public class AllTransactionsHandler extends AbstractHttpHandler {
    @Inject
    private TransactionDao transactionDao;

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        writeResponse(httpServerExchange, transactionDao.fetchAll(), StatusCodes.OK);
    }
}
