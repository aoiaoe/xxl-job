package com.xxl.job.core.enums;

public enum ScheduleType {

    NONE,

    /**
     * schedule by cron
     */
    CRON,

    /**
     * schedule by fixed rate (in seconds)
     */
    FIX_RATE,

}
