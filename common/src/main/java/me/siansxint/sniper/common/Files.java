package me.siansxint.sniper.common;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public interface Files {

    static Collection<String> loadTextFile(File file) {
        if (file == null || !file.exists()) {
            return Collections.emptySet();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}