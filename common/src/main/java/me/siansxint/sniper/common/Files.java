package me.siansxint.sniper.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface Files {

    static List<String> loadTextFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            return Collections.emptyList();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    static void writeTextFile(File file, List<String> lines) throws IOException {
        if (file == null || (!file.exists() && !file.createNewFile())) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < lines.size(); i++) {
                if (i == lines.size() - 1) {
                    writer.write(lines.get(i));
                } else {
                    writer.write(lines.get(i) + "\n");
                }
            }
        }
    }
}