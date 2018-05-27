package org.revolut.executor;

import org.revolut.dto.Transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Leo on 5/27/2018.
 */
public class OrderedLockExecutor implements TransactionalExecutor {
    private static ConcurrentHashMap<String, String> locks = new ConcurrentHashMap<>();

    @Override
    public void executeOperation(Transaction transaction,
                                 Consumer<Transaction> operation) {
        String fromAccountId = transaction.getFromAccountId();
        String toAccountId = transaction.getToAccountId();

        String firstLock = fromAccountId.compareTo(toAccountId) > 0 ? fromAccountId : toAccountId;
        String secondLock = firstLock == toAccountId ? fromAccountId : toAccountId;

        synchronized (resolveLock(firstLock)) {
            synchronized (resolveLock(secondLock)) {
                operation.accept(transaction);
            }
        }
        locks.remove(fromAccountId);
        locks.remove(toAccountId);
    }

    private String resolveLock(String firstLock) {
        String prevValue = locks.putIfAbsent(firstLock, firstLock);
        return prevValue == null ? firstLock : prevValue;
    }
}
