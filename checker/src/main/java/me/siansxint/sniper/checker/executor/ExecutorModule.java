package me.siansxint.sniper.checker.executor;

import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.common.thread.NamedVirtualThreadFactory;
import team.unnamed.inject.*;
import team.unnamed.inject.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    @Named("checker")
    public ExecutorService checkerExecutorService(Configuration configuration) {
        return Executors.newFixedThreadPool(
                configuration.poolSize(),
                new NamedVirtualThreadFactory(Thread.ofVirtual().factory())
        );
    }

    @Provides
    @Singleton
    @Named("cached")
    public ExecutorService cachedExecutorService() {
        return Executors.newCachedThreadPool(new NamedVirtualThreadFactory(Thread.ofVirtual().factory()));
    }

    @Provides
    @Singleton
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }
}