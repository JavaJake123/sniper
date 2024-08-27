package me.siansxint.sniper.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedVirtualThreadFactory implements ThreadFactory {

    private static final AtomicInteger THREAD_IDS = new AtomicInteger(1);

    private final ThreadFactory parent;

    public NamedVirtualThreadFactory(ThreadFactory parent) {
        this.parent = parent;
    }

    @Override
    public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
        Thread thread = parent.newThread(runnable);
        thread.setName("Virtual Thread " + THREAD_IDS.getAndIncrement());
        return thread;
    }
}