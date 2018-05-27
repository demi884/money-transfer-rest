package org.revoult.handler;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.revoult.utils.TestServer;

/**
 * Created by Leo on 5/27/2018.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({AccountHttpHandlersTestCases.class, TransactionHttpHandlersTestCases.class, MoneyTransferHttpHandlerTestCases.class})
public class HttpHandlerSuiteTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();
}


