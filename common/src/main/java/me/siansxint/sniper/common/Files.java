package me.siansxint.sniper.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public interface Files {

    static Collection<String> loadTextFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            return Collections.emptySet();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toSet());
        }
    }
}