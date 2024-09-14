package me.siansxint.sniper.claimer;

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
    @Inject
    @Named("refresher")
    private ExecutorService refresher;
    private @Inject ScheduledExecutorService scheduledExecutor;

    @Override
    public void start() {}

    @Override
    public void stop() {
        executor.shutdown();
        cached.shutdown();
        refresher.shutdown();
        scheduledExecutor.shutdown();
    }
}