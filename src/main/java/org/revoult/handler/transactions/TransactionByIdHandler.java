package org.revoult.handler.transactions;

import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.revoult.dao.TransactionDao;
import org.revoult.dto.Transaction;
import org.revoult.handler.AbstractHttpHandler;

import java.util.Optional;

/**
 * Created by Leo on 5/27/2018.
 */
public class TransactionByIdHandler extends AbstractHttpHandler {
    private static final String TRANSACTION_ID = "transactionId";
    @Inject
    private TransactionDao transactionDao;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String transactionId = exchange.getQueryParameters().get(TRANSACTION_ID).getFirst();
        Optional<Transaction> transactionOpt = transactionDao.byId(transactionId);
        transactionOpt.ifPresent(t -> {
            writeResponse(exchange, t, StatusCodes.OK);
        });

    }
}
