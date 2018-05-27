package org.revolut.handler;

import com.networknt.exception.ClientException;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.Before;
import org.junit.Test;
import org.revolut.dao.TransactionDao;
import org.revolut.dto.Transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Leo on 5/27/2018.
 */
public class TransactionHttpHandlersTestCases extends AbstractHttpHandlerIT {
    private String fromAccountId = "fromAccount1";
    private String toAccountId = "toAccount2";
    private Transaction testTransaction = Transaction.builder().fromAccountId(fromAccountId).toAccountId(toAccountId).status(Transaction.TransactionStatus.SUCCESS).build();

    @Before
    public void init() {
        TransactionDao.initDatabase(Collections.singletonList(testTransaction));
    }

    @Test
    public void allTransactionsBasicTest() throws ClientException, InterruptedException {
        performRequest("/v1/transactions", Methods.GET, Optional.of(StatusCodes.OK), this::validateTestTransaction);
    }

    @Test
    public void pendingTransactionsBasicTest() throws ClientException, InterruptedException {
        Transaction pendingTransaction = Transaction.builder().status(Transaction.TransactionStatus.PENDING).build();
        TransactionDao.initDatabase(Arrays.asList(testTransaction, pendingTransaction));
        performRequest("/v1/pending_transactions", Methods.GET, Optional.of(StatusCodes.OK), new Consumer<String>() {
            @Override
            public void accept(String body) {
                assertFalse(body.contains(testTransaction.getUuid().toString()));
                assertTrue(body.contains(pendingTransaction.getUuid().toString()));
                assertTrue(body.contains(Transaction.TransactionStatus.PENDING.toString()));
            }
        });
    }

    @Test
    public void byIdPositiveTest() throws ClientException, InterruptedException {
        performRequest("/v1/transactions/" + testTransaction.getUuid(), Methods.GET, Optional.of(StatusCodes.OK), this::validateTestTransaction);
    }

    @Test
    public void byIdNegativeTest() throws ClientException, InterruptedException {
        performRequest("/v1/transactions/" + "nonExistentUuid", Methods.GET, Optional.of(StatusCodes.OK), String::isEmpty);
    }

    @Test
    public void byUserIdPositiveTest() throws ClientException, InterruptedException {
        performRequest("/v1/accounts/" + testTransaction.getFromAccountId() + "/transactions", Methods.GET, Optional.of(StatusCodes.OK), this::validateTestTransaction);
        performRequest("/v1/accounts/" + testTransaction.getToAccountId() + "/transactions", Methods.GET, Optional.of(StatusCodes.OK), this::validateTestTransaction);
    }

    private void validateTestTransaction(String body) {
        assertTrue(body.contains(testTransaction.getUuid().toString()));
        assertTrue(body.contains(fromAccountId));
        assertTrue(body.contains(toAccountId));
        assertTrue(body.contains(Transaction.TransactionStatus.SUCCESS.toString()));
    }

    @Test
    public void byUserIdNegativeTest() throws ClientException, InterruptedException {
        performRequest("/v1/accounts/nonExistentUserId/transactions", Methods.GET, Optional.of(StatusCodes.OK), String::isEmpty);
    }
}
