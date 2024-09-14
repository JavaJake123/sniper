package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.common.Service;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Named;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorShutdownService implements Service {

    private @Inject ExecutorService executor;
    @Inject
    @Named("cached")
    private ExecutorService cached;
    private @Inject ScheduledExecutorService scheduledExecutor;

    @Override
    public void start() {}

    @Override
    public void stop() {
        executor.shutdown();
        cached.shutdown();
        scheduledExecutor.shutdown();
    }
}