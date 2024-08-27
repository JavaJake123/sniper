package me.siansxint.sniper.common.storage;

import me.siansxint.sniper.common.Identity;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface TStorage<T extends Identity> {

    T findSync(String id);

    CompletableFuture<T> find(String id);

    CompletableFuture<Void> save(T value);

    boolean deleteSync(String id);

    CompletableFuture<Boolean> delete(String id);

    CompletableFuture<Collection<T>> findAll();
}