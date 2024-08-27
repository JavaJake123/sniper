package me.siansxint.sniper.checker;

import me.siansxint.sniper.common.Identity;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.TStorage;

import java.util.Iterator;
import java.util.Map;

public class TCachedStorage<T extends Identity> implements TRegistry<T> {

    private final TRegistry<T> registry;
    private final TStorage<T> storage;

    public TCachedStorage(TRegistry<T> registry, TStorage<T> storage) {
        this.registry = registry;
        this.storage = storage;
    }

    @Override
    public T get(String id) {
        T model = registry.get(id);
        if (model == null) {
            model = storage.findSync(id);
        }

        return model;
    }

    @Override
    public void register(T value) {
        registry.register(value);
        storage.save(value);
    }

    @Override
    public T remove(String id) {
        T model = registry.remove(id);
        if (model == null) {
            storage.findSync(id);
        }

        storage.delete(id);

        return model;
    }

    @Override
    public long clear() {
        return registry.clear();
    }

    @Override
    public boolean empty() {
        return registry.empty();
    }

    @Override
    public Map<String, T> copy() {
        return registry.copy();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }
}