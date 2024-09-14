package me.siansxint.sniper.checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.executor.ExecutorModule;
import me.siansxint.sniper.checker.http.HttpModule;
import me.siansxint.sniper.checker.mongo.MongoConnectionModule;
import me.siansxint.sniper.checker.mongo.StorageModule;
import me.siansxint.sniper.checker.service.ChunkedNameCheckerService;
import me.siansxint.sniper.checker.service.DropTimeSanitizerService;
import me.siansxint.sniper.checker.service.ExecutorShutdownService;
import me.siansxint.sniper.checker.service.NamesService;
import me.siansxint.sniper.common.logger.LoggerModule;
import me.siansxint.sniper.common.mapper.ObjectMapperModule;
import me.siansxint.sniper.common.Service;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        install(new LoggerModule());
        install(new ExecutorModule());
        install(new ObjectMapperModule());
        install(new MongoConnectionModule());
        install(new NamesModule());
        install(new StorageModule());
        install(new HttpModule());

        multibind(Service.class)
                .asCollection(HashSet::new)
                .to(NamesService.class)
                .to(DropTimeSanitizerService.class)
                .to(ChunkedNameCheckerService.class)
                .to(ExecutorShutdownService.class)
                .singleton();
    }

    @Provides
    @Singleton
    public Configuration configuration(ObjectMapper mapper, Logger logger) {
        File config = new File("config.json");
        try {
            return mapper.readValue(config, Configuration.class);
        } catch (IOException e) {
            logger.log(
                    Level.WARNING,
                    "An error occurred while reading configuration file, using default one...",
                    e
            );

            Configuration configuration = new Configuration(
                    -1,
                    5,
                    5000,
                    10000,
                    50000,
                    "mongodb://localhost:27017"
            );
            try (Writer writer = new BufferedWriter(new FileWriter(config))) {
                mapper.writeValue(writer, configuration);
            } catch (IOException ex) {
                logger.log(
                        Level.WARNING,
                        "An error occurred while saving default configuration file, trying again...",
                        e
                );
            }

            return configuration;
        }
    }
}