package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.TStorage;
import team.unnamed.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DropTimesUploadService implements Service {

    private @Inject TRegistry<NameDropTime> dropTimeRegistry;
    private @Inject TStorage<NameDropTime> dropTimeStorage;
    private @Inject List<String> names;

    private @Inject ScheduledExecutorService scheduledExecutorService;

    private @Inject Configuration configuration;

    private @Inject Logger logger;

    @Override
    public void start() {
        long interval = configuration.savingInterval();
        scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    Iterator<NameDropTime> iterator = dropTimeRegistry.iterator();
                    while (iterator.hasNext()) {
                        NameDropTime dropTime = iterator.next();
                        dropTimeStorage.save(dropTime)
                                .whenComplete((unused, throwable) -> {
                                    if (throwable != null) {
                                        logger.log(
                                                Level.SEVERE,
                                                "Error saving drop-time for " + dropTime.id() + ".",
                                                throwable
                                        );
                                        return;
                                    }

                                    names.remove(dropTime.id());
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