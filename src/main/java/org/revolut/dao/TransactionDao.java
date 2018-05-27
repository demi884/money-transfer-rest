package org.revolut.dao;

import org.apache.commons.lang3.StringUtils;
import org.revolut.dto.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Leo on 5/27/2018.
 */
public class TransactionDao {
    private static ConcurrentHashMap<UUID, Transaction> transactions = new ConcurrentHashMap<>();

    public static void initDatabase(List<Transaction> list) {
        transactions.clear();
        list.forEach(t -> transactions.put(t.getUuid(), t));
    }

    public List<Transaction> fetchAll() {
        return transactions.values().stream().collect(Collectors.toList());
    }

    public List<Transaction> fetchPending() {
        return transactions.values().stream().filter(t -> t.getStatus() == Transaction.TransactionStatus.PENDING).collect(Collectors.toList());
    }

    public Optional<Transaction> byAccountId(String accountId) {
        if (StringUtils.isBlank(accountId)) {
            return Optional.empty();
        }

        return transactions.values().stream().filter(t ->
                StringUtils.equals(t.getFromAccountId(), accountId)
                        || StringUtils.equals(t.getToAccountId(), accountId))
                .collect(Collectors.toList()).stream().findFirst();
    }

    public Optional<Transaction> byId(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            return Optional.empty();
        }

        return transactions.values().stream().filter(t ->
                StringUtils.equals(t.getUuid().toString(), transactionId))
                .collect(Collectors.toList()).stream().findFirst();
    }

    public void logTransaction(Transaction transaction) {
        transactions.put(transaction.getUuid(), transaction);
    }


    public void markSuccessful(UUID id) {
        Transaction transaction = transactions.get(id);
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
    }

    public void markFailed(UUID id) {
        Transaction transaction = transactions.get(id);
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
    }

}
