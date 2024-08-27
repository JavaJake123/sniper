package me.siansxint.sniper.common.logger;

import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LoggerModule extends AbstractModule implements Module {

    private static final String SNIPER_NAME = "mc-sniper";

    @Provides
    @Singleton
    public Logger logger() {
        Logger logger = Logger.getLogger(SNIPER_NAME);

        File logs = new File("logs");
        if (!logs.exists() && !logs.mkdirs()) {
            return logger;
        }

        Handler fileHandler;
        try {
            fileHandler = new FileHandler("logs/latest.log", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Formatter formatter = new LoggerFormatter();

        fileHandler.setFormatter(formatter);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        logger.setUseParentHandlers(false);

        logger.addHandler(fileHandler);
        logger.addHandler(consoleHandler);

        return logger;
    }
}