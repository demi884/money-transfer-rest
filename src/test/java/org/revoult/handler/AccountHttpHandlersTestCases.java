package org.revoult.handler;

import com.networknt.exception.ClientException;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.Before;
import org.junit.Test;
import org.revoult.utils.AccountUtils;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Leo on 5/27/2018.
 */
public class AccountHttpHandlersTestCases extends AbstractHttpHandlerIT {
    private String randomExistingUserId = AccountUtils.randomAccount().getAccountId();

    @Before
    public void beforeTest() {
        AccountUtils.createAccounts(100);
    }

    @Test
    public void allAccountsBasicTest() throws ClientException, InterruptedException {
        performRequest("/v1/accounts", Methods.GET, Optional.of(StatusCodes.OK), this::validateTestAccount);
    }

    @Test
    public void byIdPositiveTest() throws ClientException, InterruptedException {
        performRequest("/v1/accounts/" + randomExistingUserId, Methods.GET, Optional.of(StatusCodes.OK), this::validateTestAccount);
    }

    @Test
    public void byIdNegativeTest() throws ClientException, InterruptedException {
        performRequest("/v1/accounts/" + "nonExistingUserId", Methods.GET, Optional.of(StatusCodes.OK), String::isEmpty);
    }

    private void validateTestAccount(String body) {
        assertTrue(body.contains(randomExistingUserId));
    }
}

