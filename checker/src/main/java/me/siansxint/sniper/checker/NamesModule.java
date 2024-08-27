package me.siansxint.sniper.checker;

import me.siansxint.sniper.checker.model.LastCheck;
import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.common.registry.LocalTRegistry;
import me.siansxint.sniper.common.registry.TRegistry;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NamesModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public List<String> names() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    @Provides
    @Singleton
    public TRegistry<NameDropTime> dropTimes() {
        return new LocalTRegistry<>();
    }

    @Provides
    @Singleton
    public TRegistry<LastCheck> lastChecks() {
        return new LocalTRegistry<>();
    }
}