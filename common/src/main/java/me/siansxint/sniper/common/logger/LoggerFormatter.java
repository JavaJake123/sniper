package me.siansxint.sniper.common.logger;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter {

    private static final String FORMAT = "[%1$tF %1$tT.%1$tL] [%2$s] [%4$s] %3$s %n";

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(FORMAT,
                new Date(record.getMillis()),
                record.getLevel().getLocalizedName(),
                record.getMessage(),
                Thread.currentThread().getName()));

        if (record.getThrown() != null) {
            sb.append("Exception: ").append(record.getThrown().getMessage()).append("\n");
        }

        return sb.toString();
    }
}