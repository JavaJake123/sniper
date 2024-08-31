package me.siansxint.sniper.checker;

import me.siansxint.sniper.checker.model.LastCheck;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.TStorage;

import java.util.Iterator;
import java.util.Map;

// this should be using redis fr
public class LastCheckCachedStorage implements TRegistry<LastCheck> {

    private final TRegistry<LastCheck> registry;
    private final TStorage<LastCheck> storage;

    public LastCheckCachedStorage(TRegistry<LastCheck> registry, TStorage<LastCheck> storage) {
        this.registry = registry;
        this.storage = storage;
    }

    @Override
    public LastCheck get(String id) {
        LastCheck model = registry.get(id);
        if (model == null) {
            model = storage.findSync(id);
        }

        return model;
    }

    @Override
    public void register(LastCheck value) {
        registry.register(value);
        storage.save(value);
    }

    @Override
    public LastCheck remove(String id) {
        LastCheck model = registry.remove(id);
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
    public int size() {
        return registry.size();
    }

    @Override
    public boolean empty() {
        return registry.empty();
    }

    @Override
    public Map<String, LastCheck> copy() {
        return registry.copy();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<LastCheck> iterator() {
        return registry.iterator();
    }
}