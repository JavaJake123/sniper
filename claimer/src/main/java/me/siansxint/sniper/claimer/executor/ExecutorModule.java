package me.siansxint.sniper.claimer.executor;

import me.siansxint.sniper.claimer.config.Configuration;
import me.siansxint.sniper.common.thread.NamedVirtualThreadFactory;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Named;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public ExecutorService executorService(Configuration configuration) {
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
    @Named("refresher")
    public ExecutorService refresherExecutorService() {
        return Executors.newFixedThreadPool(
                10,
                new NamedVirtualThreadFactory(Thread.ofVirtual().factory())
        );
    }

    @Provides
    @Singleton
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(new NamedVirtualThreadFactory(Thread.ofVirtual().factory()));
    }
}