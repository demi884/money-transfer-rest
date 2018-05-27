package org.revolut.handler.accounts;

import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.revolut.dao.AccountDao;
import org.revolut.dto.Account;
import org.revolut.handler.AbstractHttpHandler;

import java.util.Optional;

/**
 * Created by Leo on 5/27/2018.
 */
public class AccountByIdHandler extends AbstractHttpHandler {
    private static final String ACCOUNT_ID = "accountId";
    @Inject
    private AccountDao accountDao;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String transactionId = exchange.getQueryParameters().get(ACCOUNT_ID).getFirst();
        Optional<Account> accountOpt = accountDao.byId(transactionId);
        accountOpt.ifPresent(a -> {
            writeResponse(exchange, a, StatusCodes.OK);
        });
    }
}