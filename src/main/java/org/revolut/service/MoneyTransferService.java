package org.revolut.service;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import org.revolut.Settings;
import org.revolut.dao.AccountDao;
import org.revolut.dao.TransactionDao;
import org.revolut.dto.Account;
import org.revolut.dto.Transaction;
import org.revolut.executor.TransactionalExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Leo on 5/27/2018.
 */
public class MoneyTransferService {
    private static final Logger log = LoggerFactory.getLogger(MoneyTransferService.class);

    private final BlockingQueue<Transaction> pendingTransactions = new ArrayBlockingQueue<>(Settings.PENDING_TRANSACTIONS_LIMIT);
    private ExecutorService executorService = Executors.newFixedThreadPool(Settings.EXECUTORS_COUNT,
            new ThreadFactoryBuilder().setNameFormat("transactions-executor-%d").build());

    @Inject
    private AccountDao accountDao;
    @Inject
    private TransactionalExecutor transactionalExecutor;
    @Inject
    private TransactionDao transactionDao;

    @Inject
    public void init() {
        for (int i = 0; i < Settings.EXECUTORS_COUNT; i++) {
            addExecutorToPool();
        }
        executorService.shutdown();
    }

    private void addExecutorToPool() {
        executorService.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    transactionalExecutor.executeOperation(pendingTransactions.take(), (transaction) -> {
                        try {
                            Optional<Account> fromAccountOpt = accountDao.byId(transaction.getFromAccountId());
                            Optional<Account> toAccountOpt = accountDao.byId(transaction.getToAccountId());

                            Preconditions.checkArgument(fromAccountOpt.isPresent(), "Not found account: ", transaction.getFromAccountId());
                            Preconditions.checkArgument(toAccountOpt.isPresent(), "Not found account: ", transaction.getToAccountId());

                            Account fromAccount = fromAccountOpt.get();
                            Account toAccount = toAccountOpt.get();
                            BigDecimal amount = transaction.getAmount();

                            fromAccount.withdrawal(amount);
                            toAccount.deposit(amount);

                            accountDao.updateAccount(fromAccount);
                            accountDao.updateAccount(toAccount);
                            transactionDao.markSuccessful(transaction.getUuid());

                            log.info("Transaction {} performed successfully", transaction.getUuid());
                        } catch (RuntimeException e) {
                            transactionDao.markFailed(transaction.getUuid());
                            log.error("Error occurred, transaction {} marked as failed. {}", transaction.getUuid(), e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {
                    log.warn("Interrupted: ", e);
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public boolean offer(Transaction transaction) {
        transactionDao.logTransaction(transaction);
        return pendingTransactions.offer(transaction);
    }
}
