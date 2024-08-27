package me.siansxint.sniper.checker.mongo;

import com.mongodb.client.MongoClient;
import me.siansxint.sniper.checker.TCachedStorage;
import me.siansxint.sniper.checker.model.LastCheck;
import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.MongoTStorage;
import me.siansxint.sniper.common.storage.TStorage;
import team.unnamed.inject.*;
import team.unnamed.inject.Module;

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
                service
        );
    }

    @Provides
    @Singleton
    public TStorage<LastCheck> lastChecks(MongoClient client, @Named("cached") ExecutorService service) {
        return new MongoTStorage<>(
                LastCheck.class,
                DATABASE_NAME,
                "last-checks",
                client,
                service
        );
    }

    @Provides
    @Singleton
    public TCachedStorage<LastCheck> lastChecksCachedStorage(TRegistry<LastCheck> registry, TStorage<LastCheck> storage) {
        return new TCachedStorage<>(registry, storage);
    }
}