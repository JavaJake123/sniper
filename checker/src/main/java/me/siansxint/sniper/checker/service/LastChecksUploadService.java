package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.model.LastCheck;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.TStorage;
import team.unnamed.inject.Inject;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LastChecksUploadService implements Service {

    private @Inject TRegistry<LastCheck> lastCheckRegistry;
    private @Inject TStorage<LastCheck> lastCheckStorage;

    private @Inject ScheduledExecutorService scheduledExecutorService;

    private @Inject Configuration configuration;

    private @Inject Logger logger;

    @Override
    public void start() {
        long interval = configuration.savingInterval();
        scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    Iterator<LastCheck> iterator = lastCheckRegistry.iterator();
                    while (iterator.hasNext()) {
                        LastCheck dropTime = iterator.next();
                        lastCheckStorage.save(dropTime)
                                .whenComplete((unused, throwable) -> {
                                    if (throwable != null) {
                                        logger.log(
                                                Level.SEVERE,
                                                "Error saving last-check for " + dropTime.id() + ".",
                                                throwable
                                        );
                                    }
                                });
                        iterator.remove();
                    }
                },
                interval,
                interval,
                TimeUnit.MILLISECONDS
        );
    }
}