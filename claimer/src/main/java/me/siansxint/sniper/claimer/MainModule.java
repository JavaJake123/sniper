package me.siansxint.sniper.claimer;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.claimer.config.Configuration;
import me.siansxint.sniper.claimer.executor.ExecutorModule;
import me.siansxint.sniper.claimer.http.HttpModule;
import me.siansxint.sniper.claimer.mongo.MongoConnectionModule;
import me.siansxint.sniper.claimer.mongo.StorageModule;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.logger.LoggerModule;
import me.siansxint.sniper.common.mapper.ObjectMapperModule;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.logging.Logger;

public class MainModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        install(new LoggerModule());
        install(new ExecutorModule());
        install(new ObjectMapperModule());
        install(new MongoConnectionModule());
        install(new StorageModule());
        install(new HttpModule());
        install(new AccountModule());
        multibind(Service.class)
                .asCollection(HashSet::new)
                .to(ClaimerService.class)
                .singleton();
    }

    @Provides
    @Singleton
    public Configuration configuration(ObjectMapper mapper, Logger logger) {
        File config = new File("config.json");
        try {
            return mapper.readValue(config, Configuration.class);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "An error occurred while reading configuration file.",
                    e
            );
        }
    }
}