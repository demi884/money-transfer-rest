package org.revoult.handler.transactions;

import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.revoult.dao.TransactionDao;
import org.revoult.handler.AbstractHttpHandler;

/**
 * Created by Leo on 5/27/2018.
 */
public class PendingTransactionHandler extends AbstractHttpHandler {
    @Inject
    private TransactionDao transactionDao;

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        writeResponse(httpServerExchange, transactionDao.fetchPending(), StatusCodes.OK);
    }
}
