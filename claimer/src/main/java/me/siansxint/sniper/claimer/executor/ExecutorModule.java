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

public class ExecutorModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public ExecutorService provideExecutorService(Configuration configuration) {
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
}