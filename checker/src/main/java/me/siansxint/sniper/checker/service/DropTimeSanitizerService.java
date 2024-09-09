package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.common.NameDropTime;
import me.siansxint.sniper.common.ConsoleColors;
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

public class DropTimeSanitizerService implements Service {

    private @Inject TRegistry<NameDropTime> dropTimesRegistry;
    private @Inject TStorage<NameDropTime> dropTimesStorage;
    private @Inject List<String> names;

    private @Inject ScheduledExecutorService scheduledExecutorService;

    private @Inject Configuration configuration;

    private @Inject Logger logger;

    private Runnable task;

    @Override
    public void start() {
        this.task = () -> {
            // here we should check if the name is banned
            Iterator<NameDropTime> iterator = dropTimesRegistry.iterator();
            while (iterator.hasNext()) {
                NameDropTime dropTime = iterator.next();
                dropTimesStorage.save(dropTime)
                        .whenComplete((unused, throwable) -> {
                            if (throwable != null) {
                                logger.log(
                                        Level.WARNING,
                                        "Error saving drop-time for " + dropTime.id() + ".",
                                        throwable
                                );
                                return;
                            }

                            names.remove(dropTime.id());
                        });
                iterator.remove();

                logger.info(ConsoleColors.resetting(ConsoleColors.YELLOW, "Saved " + dropTime.id() + " drop-time!"));
            }
        };

        long interval = configuration.savingInterval();
        scheduledExecutorService.scheduleAtFixedRate(
                task,
                interval,
                interval,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void stop() {
        if (task != null) {
            task.run();

            logger.info("Saved all pendent drop-times!");
        }
    }
}