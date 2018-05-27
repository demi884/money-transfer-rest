package org.revoult.handler.transfer;

import com.google.inject.Inject;
import com.networknt.oas.validator.ValidationResults;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.revoult.dto.Transaction;
import org.revoult.handler.AbstractHttpHandler;
import org.revoult.service.MoneyTransferService;
import org.revoult.validator.AmountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Leo on 5/27/2018.
 */
public class MoneyTransferHttpHandler extends AbstractHttpHandler {
    public static final String FROM_USER_ID = "fromUserId";
    public static final String TO_USER_ID = "toUserId";
    public static final String AMOUNT = "amount";
    private static final Logger log = LoggerFactory.getLogger(MoneyTransferHttpHandler.class);
    @Inject
    private MoneyTransferService moneyTransferService;
    @Inject
    private AmountValidator amountValidator;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        String fromAccountId = queryParameters.get(FROM_USER_ID).getFirst();
        String toAccountId = queryParameters.get(TO_USER_ID).getFirst();
        String amountString = queryParameters.get(AMOUNT).getFirst();

        BigDecimal amount = new BigDecimal(amountString);

        if (StringUtils.equals(fromAccountId, toAccountId)) {
            writeResponse(exchange, "fromAccountId same as toAccountId", StatusCodes.BAD_REQUEST);
            return;
        }

        ValidationResults validationResults = amountValidator.validate(amountString);
        if (!validationResults.getItems().isEmpty()) {
            log.error("{}/{}/{} amount validation failed: {}", fromAccountId, toAccountId, amountString,
                    validationResults.getItems().stream().map(ValidationResults.ValidationItem::getMsg)
                            .collect(Collectors.joining(",")));
            writeResponse(exchange, validationResults, StatusCodes.BAD_REQUEST);
            return;
        }

        Transaction transaction = Transaction.builder().fromAccountId(fromAccountId).toAccountId(toAccountId).amount(amount).status(Transaction.TransactionStatus.PENDING).build();

        boolean success = moneyTransferService.offer(transaction);
        if (success) {
            writeResponse(exchange, transaction, StatusCodes.ACCEPTED);
        } else {
            log.error("{}/{}/{} skipped, queue full", fromAccountId, toAccountId, amountString);
            writeResponse(exchange, null, StatusCodes.TOO_MANY_REQUESTS);
        }
    }
}