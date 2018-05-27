package org.revoult.executor;

import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.revoult.dto.Transaction;
import org.revoult.utils.AccountUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Leo on 5/27/2018.
 */
@Ignore("Takes too much time, and there is no meaningful asserts, hence it's not a part of build yet")
public class PerformanceTest {
    private static final int OPERATION_COST_CYCLES = 10000;
    private static int accountCount;

    @Test
    public void highContentionBenchmark() throws Exception {
        accountCount = 10;
        runBenchmark();
    }

    @Test
    public void mediumContentionBenchmark() throws Exception {
        accountCount = 1000;
        runBenchmark();
    }

    @Test
    public void lowContentionBenchmark() throws Exception {
        accountCount = 100000;
        runBenchmark();
    }

    private void runBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(7)
                .threads(8)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void baseline(LockState state, Blackhole bh) {
        Object[] args = {state.transaction};
        performWork(bh, args);
    }

    @Benchmark
    public void spinLock_Benchmark(SpinLockState state, Blackhole bh) {
        state.instance.executeOperation(state.transaction, (tr) -> {
            Object[] args = {tr};
            performWork(bh, args);
        });
    }

    @Benchmark
    public void waitNotifyLock_Benchmark(WaitNotifyLockState state, Blackhole bh) {
        state.instance.executeOperation(state.transaction, (tr) -> {
            Object[] args = {tr};
            performWork(bh, args);
        });
    }

    @Benchmark
    public void orderedLock_Benchmark(OrderedLockState state, Blackhole bh) {
        state.instance.executeOperation(state.transaction, (tr) -> {
            Object[] args = {tr};
            performWork(bh, args);
        });
    }

    private void performWork(Blackhole bh, Object[] args) {
        bh.consume(args);
        Blackhole.consumeCPU(OPERATION_COST_CYCLES);
    }

    @State(Scope.Thread)
    public static class LockState {
        TransactionalExecutor instance;
        Transaction transaction;

        @Setup(Level.Trial)
        public void doSetup() {
            AccountUtils.createAccounts(accountCount);
            instance = new SpinLockExecutor();
        }

        @Setup(Level.Invocation)
        public void prepareInvocation() {
            transaction = Transaction.builder()
                    .fromAccountId(AccountUtils.randomAccount().getAccountId())
                    .toAccountId(AccountUtils.randomAccount().getAccountId())
                    .amount(AccountUtils.randomAmount())
                    .build();
        }
    }

    @State(Scope.Thread)
    public static class SpinLockState extends LockState {
        TransactionalExecutor instance;

        @Setup(Level.Trial)
        public void doSetup() {
            instance = new SpinLockExecutor();
        }
    }

    @State(Scope.Thread)
    public static class OrderedLockState extends LockState {
        TransactionalExecutor instance;

        @Setup(Level.Trial)
        public void doSetup() {
            instance = new OrderedLockExecutor();
        }
    }

    @State(Scope.Thread)
    public static class WaitNotifyLockState extends LockState {
        TransactionalExecutor instance;

        @Setup(Level.Trial)
        public void doSetup() {
            instance = new WaitNotifyExecutor();
        }
    }
}