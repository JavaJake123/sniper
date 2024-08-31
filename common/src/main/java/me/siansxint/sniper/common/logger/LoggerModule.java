package me.siansxint.sniper.common.logger;

import team.unnamed.inject.*;
import team.unnamed.inject.Module;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerModule extends AbstractModule implements Module {

    private static final String SNIPER_NAME = "mc-sniper";

    @Provides
    @Singleton
    public Logger logger(@Named("cached") ExecutorService service) {
        Logger logger = Logger.getLogger(SNIPER_NAME);

        File logs = new File("logs");
        if (!logs.exists() && !logs.mkdirs()) {
            return logger;
        }

        Handler fileHandler;
        try {
            fileHandler = new FileHandler("logs/latest.log", true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Formatter formatter = new LoggerFormatter();

        fileHandler.setFormatter(formatter);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        logger.setUseParentHandlers(false);

        logger.addHandler(fileHandler);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                service.submit(() -> consoleHandler.publish(record));
            }

            @Override
            public void flush() {
                consoleHandler.flush();
            }

            @Override
            public void close() throws SecurityException {
                consoleHandler.close();
            }
        });

        return logger;
    }
}