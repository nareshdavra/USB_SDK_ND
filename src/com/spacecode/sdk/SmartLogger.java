//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spacecode.sdk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class SmartLogger extends Logger {
    private static final SmartLogger LOGGER = new SmartLogger("SmartLogger", (String)null);

    protected SmartLogger(String name, String resourceBundleName) throws MissingResourceException {
        super(name, resourceBundleName);
        SmartLogger.ShortFormatter formatter = new SmartLogger.ShortFormatter();
        SmartLogger.SmartConsoleHandler consoleHandler = new SmartLogger.SmartConsoleHandler();
        consoleHandler.setLevel(Level.OFF);
        consoleHandler.setFormatter(formatter);
        this.addHandler(consoleHandler);
    }

    public static SmartLogger getLogger() {
        return LOGGER;
    }

    static {
        LogManager.getLogManager().reset();
    }

    static class SmartConsoleHandler extends ConsoleHandler {
        public SmartConsoleHandler() {
            super.setOutputStream(System.out);
        }
    }

    static class ShortFormatter extends Formatter {
        private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ShortFormatter() {
        }

        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.df.format(new Date(record.getMillis()))).append(" ");
            builder.append("[").append(record.getLevel()).append("] ");
            builder.append(this.formatMessage(record));
            builder.append("\n");
            if(record.getThrown() != null) {
                builder.append(record.getThrown().getMessage());
                builder.append("\n");
            }

            return builder.toString();
        }
    }
}
