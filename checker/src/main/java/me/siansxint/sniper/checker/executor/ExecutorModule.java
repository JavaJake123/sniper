package me.siansxint.sniper.checker.executor;

import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.common.http.HttpClientSelector;
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
    public ExecutorService checkerExecutorService(Configuration configuration, HttpClientSelector selector) {
        return Executors.newFixedThreadPool(
                configuration.poolSize() == -1 ? selector.size() : configuration.poolSize(),
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
        return Executors.newScheduledThreadPool(1);
    }
}