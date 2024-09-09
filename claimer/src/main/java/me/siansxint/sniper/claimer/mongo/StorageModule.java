package me.siansxint.sniper.claimer.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Indexes;
import me.siansxint.sniper.common.NameDropTime;
import me.siansxint.sniper.common.storage.MongoTStorage;
import me.siansxint.sniper.common.storage.TStorage;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Named;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.util.concurrent.ExecutorService;

public class StorageModule extends AbstractModule implements Module {

    private static final String DATABASE_NAME = "sniper";

    @Provides
    @Singleton
    public TStorage<NameDropTime> dropTimes(MongoClient client, @Named("cached") ExecutorService service) {
        return new MongoTStorage<>(
                NameDropTime.class,
                DATABASE_NAME,
                "drop-times",
                client,
                service,
                collection -> collection.createIndex(Indexes.descending("from"))
        );
    }
}