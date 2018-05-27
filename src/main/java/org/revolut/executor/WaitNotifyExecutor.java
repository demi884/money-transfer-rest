package org.revolut.executor;

import org.revolut.dto.Transaction;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Leo on 5/27/2018.
 */
public class WaitNotifyExecutor implements TransactionalExecutor {
    private final Set<String> lockedAccounts = new HashSet<>();

    @Override
    public void executeOperation(Transaction transaction,
                                 Consumer<Transaction> operation) {
        String fromAccountId = transaction.getFromAccountId();
        String toAccountId = transaction.getToAccountId();
        lock(fromAccountId, toAccountId);
        try {
            operation.accept(transaction);
        } finally {
            unlock(fromAccountId, toAccountId);
        }
    }

    private void lock(String fromAccountId, String toAccountId) {
        synchronized (lockedAccounts) {
            while (!Thread.currentThread().isInterrupted() &&
                    (lockedAccounts.contains(fromAccountId) || lockedAccounts.contains(toAccountId))) {
                try {
                    lockedAccounts.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lockedAccounts.add(fromAccountId);
            lockedAccounts.add(toAccountId);
        }
    }

    private synchronized void unlock(String fromAccountId, String toAccountId) {
        synchronized (lockedAccounts) {
            lockedAccounts.remove(fromAccountId);
            lockedAccounts.remove(toAccountId);
            lockedAccounts.notifyAll();
        }
    }
}
