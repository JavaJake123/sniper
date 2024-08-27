package me.siansxint.sniper.claimer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

// testing purposes just by now
public class ClaimerMain {

    private static final char[] CHARACTERS = "0123456789_abcdefghijklmnopqrstuvwxyz".toCharArray();

    public static void main(String[] args) {
        String[] combinations = new String[50653];

        int index = 0;
        for (char a : CHARACTERS) {
            for (char b : CHARACTERS) {
                for (char c : CHARACTERS) {
                    combinations[index++] = "" + a + b + c;
                }
            }
        }

        File file = new File("names.txt");
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            for (String name : combinations) {
                System.out.println(name);
                writer.write(name + "\n");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}