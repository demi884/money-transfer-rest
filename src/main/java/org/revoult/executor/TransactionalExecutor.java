package org.revoult.executor;

import org.revoult.dto.Transaction;

import java.util.function.Consumer;

/**
 * Created by Leo on 5/27/2018.
 */
public interface TransactionalExecutor {
    void executeOperation(Transaction transaction,
                          Consumer<Transaction> operation);
}
