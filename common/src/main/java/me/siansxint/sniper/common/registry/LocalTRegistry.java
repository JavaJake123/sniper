package me.siansxint.sniper.common.registry;

import me.siansxint.sniper.common.Identity;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalTRegistry<T extends Identity> implements TRegistry<T> {

    private final Map<String, T> cached = Collections.synchronizedMap(new ConcurrentHashMap<>());

    @Override
    public T get(String id) {
        return cached.get(id);
    }

    @Override
    public void register(T value) {
        cached.put(value.id(), value);
    }

    @Override
    public T remove(String id) {
        return cached.remove(id);
    }

    @Override
    public long clear() {
        long size = cached.size();
        cached.clear();
        return size;
    }

    @Override
    public boolean empty() {
        return cached.isEmpty();
    }

    @Override
    public Map<String, T> copy() {
        return Map.copyOf(cached);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<T> iterator() {
        return cached.values().iterator();
    }
}