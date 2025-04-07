package com.techstud.schedule_university.auth.util;

import java.time.Duration;

public class ConstantsUtil {
    public static final Duration RESEND_INTERVAL = Duration.ofMinutes(2);
    public static final int CLEANUP_BATCH_SIZE = 1000;
    public static final int FIVE_MINUTES_IN_MS = 15 * 60 * 1000;
    public static final int NO_ROWS_UPDATED = 0;
}
