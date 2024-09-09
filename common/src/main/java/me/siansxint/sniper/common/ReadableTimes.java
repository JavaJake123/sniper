package me.siansxint.sniper.common;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public final class ReadableTimes {

    public static String durationToHumanTime(long time) {
        StringJoiner joiner = new StringJoiner(" ");

        long seconds = time / 1000;

        int unitValue = Math.toIntExact(seconds / TimeUnit.DAYS.toSeconds(7));
        if (unitValue > 0) {
            seconds %= TimeUnit.DAYS.toSeconds(7);

            String unit;

            if (unitValue == 1) {
                unit = "week";
            } else {
                unit = "weeks";
            }

            joiner.add(unitValue + " " + unit);
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.DAYS.toSeconds(1));
        if (unitValue > 0) {
            seconds %= TimeUnit.DAYS.toSeconds(1);

            String unit;

            if (unitValue == 1) {
                unit = "day";
            } else {
                unit = "days";
            }

            joiner.add(unitValue + " " + unit);
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.HOURS.toSeconds(1));
        if (unitValue > 0) {
            seconds %= TimeUnit.HOURS.toSeconds(1);
            String unit;

            if (unitValue == 1) {
                unit = "hour";
            } else {
                unit = "hours";
            }

            joiner.add(unitValue + " " + unit);
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.MINUTES.toSeconds(1));
        if (unitValue > 0) {
            seconds %= TimeUnit.MINUTES.toSeconds(1);
            String unit;

            if (unitValue == 1) {
                unit = "minute";
            } else {
                unit = "minutes";
            }

            joiner.add(unitValue + " " + unit);
        }

        if (seconds > 0 || joiner.length() == 0) {
            String unit;

            if (seconds == 1) {
                unit = "second";
            } else {
                unit = "seconds";
            }

            joiner.add(seconds + " " + unit);
        }

        return joiner.toString();
    }
}