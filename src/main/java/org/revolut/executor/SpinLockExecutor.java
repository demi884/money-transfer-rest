package org.revolut.executor;

import org.revolut.dto.Transaction;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Leo on 5/27/2018.
 */
public class SpinLockExecutor implements TransactionalExecutor {
    private Set<String> lockedUsers = new HashSet<>();

    @Override
    public void executeOperation(Transaction transaction,
                                 Consumer<Transaction> operation) {
        String fromAccountId = transaction.getFromAccountId();
        String toAccountId = transaction.getToAccountId();

        lock(fromAccountId, toAccountId);
        operation.accept(transaction);
        unlock(fromAccountId, toAccountId);
    }

    private void lock(String fromAccountId, String toAccountId) {
        while (lockedUsers.contains(fromAccountId)
                || lockedUsers.contains(toAccountId)) {
        }
        lockedUsers.add(fromAccountId);
        lockedUsers.add(toAccountId);
    }

    private synchronized void unlock(String fromUserId, String toUserId) {
        lockedUsers.remove(fromUserId);
        lockedUsers.remove(toUserId);
    }
}
