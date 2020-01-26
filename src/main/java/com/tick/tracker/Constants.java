package com.tick.tracker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
/**
 * 
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 * 
 * All the application specific configurations are defined here
 * All Constants used in the analysis component
 *
 */
public final class Constants {
    public static final int THREAD_COUNT=10;

    public static final int SUBSCRIBER_BUFFER_SIZE = THREAD_COUNT*3;
    
    /**
     * 
     * All threading related constants are defined here
     *
     */
    public static final class Threading {

        public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(THREAD_COUNT);
        public static final ScheduledExecutorService EXPIRED_TICKS_SCHEDULER = Executors.newScheduledThreadPool(THREAD_COUNT);
        public static final long SCHEDULE_DELAY = 500l;
        public static final long AWAIT_TERMINATION = 1000l;
        public static final long DELAY_WINDOW=60000;  


    }
}
