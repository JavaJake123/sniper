package me.siansxint.sniper.common.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.siansxint.sniper.common.Identity;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class MongoTStorage<T extends Identity> implements TStorage<T> {

    private static final ReplaceOptions OPTIONS = new ReplaceOptions().upsert(true);

    private final MongoCollection<T> collection;

    private final ExecutorService executor;

    public MongoTStorage(
            Class<T> type,
            String database,
            String collection,
            MongoClient client,
            ExecutorService executor,
            Consumer<MongoCollection<T>> modifier
    ) {
        this.collection = client.getDatabase(database).getCollection(collection, type);
        modifier.accept(this.collection);
        this.executor = executor;
    }

    @Override
    public T findSync(String id) {
        return collection.find(Filters.eq("_id", id)).first();
    }

    @Override
    public CompletableFuture<T> find(String id) {
        return CompletableFuture.supplyAsync(() -> findSync(id), executor);
    }

    @Override
    public CompletableFuture<Void> save(T value) {
        return CompletableFuture.runAsync(() -> collection.replaceOne(
                Filters.eq("_id", value.id()),
                value,
                OPTIONS
        ), executor);
    }

    @Override
    public boolean deleteSync(String id) {
        return collection.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }

    @Override
    public CompletableFuture<Boolean> delete(String id) {
        return CompletableFuture.supplyAsync(() -> deleteSync(id), executor);
    }

    @Override
    public CompletableFuture<Collection<T>> findAll() {
        return CompletableFuture.supplyAsync(() -> collection.find().into(new HashSet<>()));
    }

    public CompletableFuture<Collection<T>> findAllSorting(Bson filter) {
        return CompletableFuture.supplyAsync(() -> collection.find().sort(filter).into(new ArrayList<>()));
    }
}