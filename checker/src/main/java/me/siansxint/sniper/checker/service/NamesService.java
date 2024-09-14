package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.Service;
import team.unnamed.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NamesService implements Service {

    private @Inject List<String> names;

    @Override
    public void start() {}

    @Override
    public void stop() {
        File file = new File("names.txt");
        try {
            Files.writeTextFile(file, names);
            System.out.println("Saved names!");
        } catch (IOException e) {
            System.err.println("An error occurred while trying to save names. Exception message: " + e.getMessage());
        }
    }
}