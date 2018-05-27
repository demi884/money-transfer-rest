package org.revolut.handler;

import com.networknt.client.Http2Client;
import com.networknt.exception.ClientException;
import io.undertow.UndertowOptions;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.HttpString;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.revolut.handler.transfer.MoneyTransferHttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Leo on 5/27/2018.
 */
public class AbstractHttpHandlerIT {
    protected static final Consumer<String> noOp = s -> {
    };
    static final int N_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpHandlerIT.class);
    private static final boolean enableHttp2 = HttpHandlerSuiteTest.server.getServerConfig().isEnableHttp2();
    private static final boolean enableHttps = HttpHandlerSuiteTest.server.getServerConfig().isEnableHttps();
    private static final int httpPort = HttpHandlerSuiteTest.server.getServerConfig().getHttpPort();
    private static final int httpsPort = HttpHandlerSuiteTest.server.getServerConfig().getHttpsPort();
    private static final String url = enableHttp2 || enableHttps ? "https://localhost:" + httpsPort : "http://localhost:" + httpPort;
    private static BlockingQueue<AbstractHttpHandlerIT.WrappedConnection> pool;

    @BeforeClass
    public static void initClass() throws ClientException {
        pool = new ArrayBlockingQueue<>(N_THREADS);
        for (int i = 0; i < N_THREADS; i++) {
            AbstractHttpHandlerIT.WrappedConnection wrappedConnection = new AbstractHttpHandlerIT.WrappedConnection().invoke();
            pool.offer(wrappedConnection);
        }
    }

    @AfterClass
    public static void tearDown() throws ClientException {
        for (AbstractHttpHandlerIT.WrappedConnection wrappedConnection : pool) {
            IoUtils.safeClose(wrappedConnection.getConnection());
        }
    }

    protected void performRequest(String url, HttpString method, Optional<Integer> expectedResponseCode, Consumer<String> bodyValidationFunction) throws ClientException, InterruptedException {
        AbstractHttpHandlerIT.WrappedConnection wrappedConnection = null;
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            wrappedConnection = pool.take();
            Http2Client client = wrappedConnection.getClient();

            ClientRequest request = new ClientRequest().setPath(url).setMethod(method);
            final CountDownLatch latch = new CountDownLatch(1);
            wrappedConnection.getConnection().sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            if (wrappedConnection != null) {
                pool.put(wrappedConnection);
            }
        }

        final ClientResponse clientResponse = reference.get();
        expectedResponseCode.ifPresent(responseCode -> assertEquals(responseCode.intValue(), clientResponse.getResponseCode()));

        String body = clientResponse.getAttachment(Http2Client.RESPONSE_BODY);
        bodyValidationFunction.accept(body);
    }

    private String buildQueryLine(String fromUserId, String toUserId, double amount) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        params.put(MoneyTransferHttpHandler.FROM_USER_ID, fromUserId);
        params.put(MoneyTransferHttpHandler.TO_USER_ID, toUserId);
        params.put(MoneyTransferHttpHandler.AMOUNT, String.valueOf(amount));
        return Http2Client.getFormDataString(params);
    }

    private static class WrappedConnection {
        private Http2Client client;
        private ClientConnection connection;

        public Http2Client getClient() {
            return client;
        }

        public ClientConnection getConnection() {
            return connection;
        }

        public AbstractHttpHandlerIT.WrappedConnection invoke() throws ClientException {
            client = Http2Client.getInstance();

            try {
                connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, enableHttp2 ? OptionMap.create(UndertowOptions.ENABLE_HTTP2, true) : OptionMap.EMPTY).get();
            } catch (Exception e) {
                throw new ClientException(e);
            }
            return this;
        }
    }
}
