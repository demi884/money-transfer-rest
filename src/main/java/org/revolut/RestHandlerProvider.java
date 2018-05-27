package org.revolut;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;
import org.revolut.handler.accounts.AccountByIdHandler;
import org.revolut.handler.accounts.AllAccountsHandler;
import org.revolut.handler.transactions.AllTransactionsHandler;
import org.revolut.handler.transactions.PendingTransactionHandler;
import org.revolut.handler.transactions.TransactionByAccountIdHandler;
import org.revolut.handler.transactions.TransactionByIdHandler;
import org.revolut.handler.transfer.MoneyTransferHttpHandler;

/**
 * Created by Leo on 5/27/2018.
 */
public class RestHandlerProvider implements HandlerProvider {
    private static Injector injector = Guice.createInjector(new Settings());

    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()
                .add(Methods.POST, "/v1/transfer", injector.getInstance(MoneyTransferHttpHandler.class))
                .add(Methods.GET, "/v1/accounts", injector.getInstance(AllAccountsHandler.class))
                .add(Methods.GET, "/v1/transactions", injector.getInstance(AllTransactionsHandler.class))
                .add(Methods.GET, "/v1/pending_transactions", injector.getInstance(PendingTransactionHandler.class))
                .add(Methods.GET, "/v1/transactions/{transactionId}", injector.getInstance(TransactionByIdHandler.class))
                .add(Methods.GET, "/v1/accounts/{accountId}/transactions", injector.getInstance(TransactionByAccountIdHandler.class))
                .add(Methods.GET, "/v1/accounts/{accountId}", injector.getInstance(AccountByIdHandler.class));

    }
}