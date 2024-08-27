package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.Service;
import team.unnamed.inject.Inject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

public class NamesService implements Service {

    private @Inject List<String> names;

    @Override
    public void start() {
        names.addAll(new HashSet<>(Files.loadTextFile(new File("names.txt")))); // this collection is synchronized, actually needed???
    }

    @Override
    public void stop() {
        File file = new File("names.txt");
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < names.size(); i++) {
                if (i == names.size() - 1) {
                    writer.write(names.get(i));
                } else {
                    writer.write(names.get(i) + "\n");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}