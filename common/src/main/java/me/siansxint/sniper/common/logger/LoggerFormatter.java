package me.siansxint.sniper.common.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter {

    private static final String FORMAT = "[%1$tF %1$tT.%1$tL] [%2$s] %3$s %n";

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(FORMAT,
                new Date(record.getMillis()),
                record.getLevel().getLocalizedName(),
                record.getMessage()));

        if (record.getThrown() != null) {
            String throwable;

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();

            builder.append("Exception: ").append(throwable).append("\n");
        }

        return builder.toString();
    }
}