package me.siansxint.sniper.checker.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import me.siansxint.sniper.checker.config.Configuration;
import net.sintaxis.codec.MongoJacksonCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoConnectionModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public MongoClient mongoClient(Configuration configuration, ObjectMapper mapper) {
        MongoClient client = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(configuration.mongoUri()))
                        .codecRegistry(CodecRegistries.fromRegistries(
                                MongoClientSettings.getDefaultCodecRegistry(),
                                CodecRegistries.fromProviders(new MongoJacksonCodecProvider(mapper))
                        ))
                        .build()
        );

        Logger.getLogger("org.mongodb.driver.client")
                .setLevel(Level.SEVERE);

        return client;
    }
}