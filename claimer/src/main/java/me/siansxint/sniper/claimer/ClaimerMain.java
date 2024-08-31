package me.siansxint.sniper.claimer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// testing purposes just by now
public class ClaimerMain {

    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz_0123456789".toCharArray();

    public static void main1(String[] args) {
        String[] combinations = new String[50653];

        int index = 0;
        for (char i : CHARS) {
            for (char j : CHARS) {
                for (char k : CHARS) {
                    System.out.println(index);
                    combinations[index++] = "" + i + j + k;
                }
            }
        }

        try (Writer writer = new BufferedWriter(new FileWriter("names.txt"))) {
            for (int i = 0; i < combinations.length; i++) {
                if (i == combinations.length - 1) {
                    writer.write(combinations[i]);
                } else {
                    writer.write(combinations[i] + "\n");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.nanoTime();
        Instant from = Instant.ofEpochSecond((long) 1724974437.8757012);
        Instant to = Instant.ofEpochSecond((long) 1724980298.33745);

        ZoneId zoneId = ZoneId.of("GMT-4");

        System.out.println(from);
        System.out.println(to);

        Thread.sleep(10000);

        System.out.println(from.plus(37, ChronoUnit.DAYS).atZone(zoneId).toLocalDateTime());
        System.out.println(to.plus(37, ChronoUnit.DAYS).atZone(zoneId).toLocalDateTime());

        System.out.println(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
    }
}