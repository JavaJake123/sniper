package me.siansxint.sniper.checker;

import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.registry.LocalTRegistry;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.TStorage;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NamesModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public List<String> names(TStorage<NameDropTime> dropTimes) {
        Set<String> names = new HashSet<>(Files.loadTextFile(new File("names.txt")));
        // remove already dropping names, this could be necessary when names file is not updated when finishing the program
        dropTimes.findAll()
                .join()
                .forEach(dropTime -> names.remove(dropTime.id()));
        return Collections.synchronizedList(new ArrayList<>(names));
    }

    @Provides
    @Singleton
    public TRegistry<NameDropTime> dropTimes() {
        return new LocalTRegistry<>();
    }
}