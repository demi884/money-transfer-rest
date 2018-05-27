package org.revolut;

import com.google.inject.AbstractModule;
import org.revolut.executor.TransactionalExecutor;
import org.revolut.executor.WaitNotifyExecutor;

public class Settings extends AbstractModule {
    public static final int EXECUTORS_COUNT = 4;
    public static final int PENDING_TRANSACTIONS_LIMIT = 1000;

    @Override
    protected void configure() {
        bind(TransactionalExecutor.class).to(WaitNotifyExecutor.class);
    }
}