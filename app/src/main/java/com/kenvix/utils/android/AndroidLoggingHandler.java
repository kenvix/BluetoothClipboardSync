package com.kenvix.utils.android;

import android.util.Log;

import com.kenvix.utils.log.LogSettings;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * <p>Java Logger handler for Android.</p>
 * If you want to use Kenvix Logger in android project, you need to use this handler.
 * Use it by calling {@link AndroidLoggingHandler#applyToKenvixLogger()}
 */
public class AndroidLoggingHandler extends Handler {

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
        if (!super.isLoggable(record))
            return;

        String name = record.getLoggerName();
        int maxLength = 30;
        String tag = name.length() > maxLength ? name.substring(name.length() - maxLength) : name;

        try {
            int level = getAndroidLevel(record.getLevel());
            Log.println(level, tag, record.getMessage());
            if (record.getThrown() != null) {
                Log.println(level, tag, Log.getStackTraceString(record.getThrown()));
            }
        } catch (RuntimeException e) {
            Log.e("AndroidLoggingHandler", "Error logging message.", e);
        }
    }

    private static int getAndroidLevel(Level level) {
        int value = level.intValue();

        if (value >= Level.SEVERE.intValue()) {
            return Log.ERROR;
        } else if (value >= Level.WARNING.intValue()) {
            return Log.WARN;
        } else if (value >= Level.INFO.intValue()) {
            return Log.INFO;
        } else if (value >= Level.FINER.intValue()) {
            return Log.VERBOSE;
        } else {
            return Log.DEBUG;
        }
    }

    public static void resetLogger(Handler rootHandler) {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.addHandler(rootHandler);
    }

    public static void applyToKenvixLogger() {
        LogSettings.setUseConsoleHandler(false);
        LogSettings.addHandler(new AndroidLoggingHandler());
    }
}