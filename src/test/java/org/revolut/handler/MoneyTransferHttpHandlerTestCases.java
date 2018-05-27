package org.revolut.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.networknt.client.Http2Client;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.Test;
import org.revolut.dao.AccountDao;
import org.revolut.dto.Account;
import org.revolut.dto.Transaction;
import org.revolut.handler.transfer.MoneyTransferHttpHandler;
import org.revolut.utils.AccountUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static junit.framework.TestCase.assertEquals;

public class MoneyTransferHttpHandlerTestCases extends AbstractHttpHandlerIT {
    private static final Logger logger = LoggerFactory.getLogger(MoneyTransferHttpHandlerTestCases.class);

    @Test
    public void sumShouldBeConstant() throws ClientException, ApiException, InterruptedException {
        AccountUtils.createAccounts(10);

        BigDecimal sumBefore = AccountDao.sum();
        final AtomicInteger exceptionCount = new AtomicInteger();
        ExecutorService service = Executors.newFixedThreadPool(N_THREADS);
        for (int i = 0; i < 300; i++) {
            service.execute(() -> {
                try {
                    String fromAccountId = AccountUtils.randomAccount().getAccountId();
                    String toAccountId = AccountUtils.randomAccount().getAccountId();
                    while (fromAccountId.equals(toAccountId)) {
                        toAccountId = AccountUtils.randomAccount().getAccountId();
                    }

                    performMoneyTransferRequest(fromAccountId, toAccountId, AccountUtils.randomAccount().getBalance().doubleValue());
                } catch (Throwable e) { //AssertionFailedError is a reason of Throwable here
                    logger.error("Exception: ", e);
                    exceptionCount.incrementAndGet();
                }
            });
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);

        assertNoPendingTransactionsRemain();

        BigDecimal sumAfter = AccountDao.sum();
        assertEquals(sumBefore, sumAfter);
        assertEquals("No exceptions expected during the test", 0, exceptionCount.get());
    }

    @Test
    public void fromAccountIdEqualsWithToAccountId() throws ClientException, InterruptedException, UnsupportedEncodingException {
        Account account = AccountUtils.randomAccount();
        String url = "/v1/transfer?" + buildQueryLine(account.getAccountId(), account.getAccountId(), account.getBalance().doubleValue());
        performRequest(url, Methods.POST, Optional.of(StatusCodes.BAD_REQUEST), noOp);
    }

    @Test
    public void transferCompleteScenario() throws ClientException, InterruptedException, UnsupportedEncodingException {
        AccountDao.initDatabase(Arrays.asList(
                Account.builder().accountId("id1").balance(BigDecimal.valueOf(100.1)).build(),
                Account.builder().accountId("id2").balance(BigDecimal.valueOf(10.1)).build()
        ));

        StringBuilder body = new StringBuilder();
        performRequest("/v1/transfer?" + buildQueryLine("id1", "id2", 10.05), Methods.POST, Optional.of(StatusCodes.ACCEPTED), new Consumer<String>() {
            @Override
            public void accept(String s) {
                body.append(s);
            }
        });
        Transaction pendingTransaction = new Gson().fromJson(body.toString(), Transaction.class);

        assertNoPendingTransactionsRemain();

        Transaction successfulTransaction = requestTransaction(pendingTransaction.getUuid());
        assertEquals(Transaction.TransactionStatus.SUCCESS, successfulTransaction.getStatus());
        assertEquals(requestAccount("id1").getBalance(), BigDecimal.valueOf(90.05));
        assertEquals(requestAccount("id2").getBalance(), BigDecimal.valueOf(20.15));
    }

    private Transaction requestTransaction(UUID transactionId) throws ClientException, InterruptedException {
        StringBuilder transactionStatus = new StringBuilder();
        performRequest("/v1/transactions/" + transactionId, Methods.GET, Optional.of(StatusCodes.OK), new Consumer<String>() {
            @Override
            public void accept(String s) {
                transactionStatus.append(s);
            }
        });
        return new Gson().fromJson(transactionStatus.toString(), Transaction.class);
    }

    private void assertNoPendingTransactionsRemain() throws ClientException, InterruptedException {
        int attempts = 0;
        int pendingTransactionsCount = getPendingTransactionsCount();
        while (pendingTransactionsCount != 0 && attempts < 3) {
            pendingTransactionsCount = getPendingTransactionsCount();
            attempts++;
            Thread.sleep(100);
        }
        assertEquals("Pending transactions found after " + attempts + " attempts", 0, pendingTransactionsCount);
    }

    private int getPendingTransactionsCount() throws ClientException, InterruptedException {
        StringBuilder pendingTransactions = new StringBuilder();
        performRequest("/v1/pending_transactions", Methods.GET, Optional.of(StatusCodes.OK), pendingTransactions::append);
        List<Transaction> pendingTransactionList = new Gson()
                .fromJson(pendingTransactions.toString(), new TypeToken<ArrayList<Transaction>>() {
                }.getType());

        return pendingTransactionList.size();
    }

    private Account requestAccount(String accountId) throws ClientException, InterruptedException {
        StringBuilder account1 = new StringBuilder();
        performRequest("/v1/accounts/" + accountId, Methods.GET, Optional.of(StatusCodes.OK), new Consumer<String>() {
            @Override
            public void accept(String s) {
                account1.append(s);
            }
        });
        return new Gson().fromJson(account1.toString(), Account.class);
    }

    private void performMoneyTransferRequest(String fromUserId, String toUserId, double amount) throws UnsupportedEncodingException, InterruptedException, ClientException {
        String url = "/v1/transfer?" + buildQueryLine(fromUserId, toUserId, amount);
        performRequest(url, Methods.POST, Optional.of(StatusCodes.ACCEPTED), noOp);
    }

    private String buildQueryLine(String fromUserId, String toUserId, double amount) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        params.put(MoneyTransferHttpHandler.FROM_USER_ID, fromUserId);
        params.put(MoneyTransferHttpHandler.TO_USER_ID, toUserId);
        params.put(MoneyTransferHttpHandler.AMOUNT, String.valueOf(amount));
        return Http2Client.getFormDataString(params);
    }

}