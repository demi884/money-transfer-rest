package org.revolut.executor;

import org.revolut.dto.Transaction;

import java.util.function.Consumer;

/**
 * Created by Leo on 5/27/2018.
 */
public interface TransactionalExecutor {
    void executeOperation(Transaction transaction,
                          Consumer<Transaction> operation);
}
