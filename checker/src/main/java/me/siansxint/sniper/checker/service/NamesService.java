package me.siansxint.sniper.checker.service;

import me.siansxint.sniper.common.Service;
import team.unnamed.inject.Inject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NamesService implements Service {

    private @Inject List<String> names;

    private @Inject Logger logger;

    @Override
    public void start() {}

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

            logger.info("Saved names!");
        } catch (IOException e) {
            logger.log(
                    Level.WARNING,
                    "An error occurred while saving names...",
                    e
            );
        }
    }
}