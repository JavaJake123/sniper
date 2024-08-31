package me.siansxint.sniper.common.registry;

import me.siansxint.sniper.common.Identity;

import java.util.Map;

public interface TRegistry<T extends Identity> extends Iterable<T> {

    T get(String id);

    void register(T value);

    T remove(String id);

    long clear();

    int size();

    boolean empty();

    Map<String, T> copy();
}