package com.wsun.javabase.thread.guava;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MyExecutor extends AbstractExecutorService {

    BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();

    AtomicInteger ctl = new AtomicInteger(1);

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        int c = ctl.get();
        if (workerCountOf(c) < 10) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (workQueue.offer(command)) {

        }
        else if (!addWorker(command, false)) {

        }
    }

    private boolean addWorker(Runnable command, boolean b) {
        return true;
    }


    private static int workerCountOf(int c)  { return 0; }
}
