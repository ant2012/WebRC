package ru.ant.iot;

import org.apache.log4j.Logger;
import ru.ant.common.App;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ant on 16.05.2016.
 */
public class TriggerPoolManager {
    private static final String SCHEDULER_PROPERTY_TEMPLATE = "TriggerPoolManager.%1$s.%2$s";

    private static String getProp(String className, String key) {
        String fullKey = String.format(SCHEDULER_PROPERTY_TEMPLATE, className, key);
        String defaultKey = String.format(SCHEDULER_PROPERTY_TEMPLATE, "default", key);
        String result = App.getProperty(fullKey);
        return result != null ? result : App.getProperty(defaultKey);
    }

    public static void addTrigger(ScheduledExecutorService pool, Runnable trigger) {
        String className = trigger.getClass().getSimpleName();
        int initialDelay = Integer.parseInt(getProp(className, "initialDelay"));
        int delay = Integer.parseInt(getProp(className, "delay"));
        Logger.getLogger(TriggerPoolManager.class).info(String.format("Sheduler started for: %1$s; initialDelay=%2$ss; delay=%3$ss", className, initialDelay, delay));
        pool.scheduleWithFixedDelay(trigger, initialDelay, delay, TimeUnit.SECONDS);
    }
}
