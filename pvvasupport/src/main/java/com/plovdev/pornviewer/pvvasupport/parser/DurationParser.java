package com.plovdev.pornviewer.pvvasupport.parser;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    private static final Pattern pattern = Pattern.compile("^(?:(\\d+):)?(?:(\\d+):)?(?:(\\d+):)?(\\d+)(?:\\.(\\d{1,9}))?$");
    private DurationParser() {
    }

    public static Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) {
            return Duration.ZERO;
        }

        durationStr = durationStr.trim().replace(',', '.');
        if (durationStr.startsWith("PT")) {
            return Duration.parse(durationStr);
        }

        Matcher matcher = pattern.matcher(durationStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + durationStr);
        }

        String[] values = new String[4];
        int idx = 0;
        for (int i = 1; i <= 4; i++) {
            if (matcher.group(i) != null) {
                values[idx++] = matcher.group(i);
            }
        }

        long days = 0, hours = 0, minutes = 0, seconds = 0;
        int len = idx;

        if (len == 4) {
            days = Long.parseLong(values[0]);
            hours = Long.parseLong(values[1]);
            minutes = Long.parseLong(values[2]);
            seconds = Long.parseLong(values[3]);
        } else if (len == 3) {
            hours = Long.parseLong(values[0]);
            minutes = Long.parseLong(values[1]);
            seconds = Long.parseLong(values[2]);
        } else if (len == 2) {
            minutes = Long.parseLong(values[0]);
            seconds = Long.parseLong(values[1]);
        } else if (len == 1) {
            seconds = Long.parseLong(values[0]);
        }

        long nanos = 0;
        if (matcher.group(5) != null) {
            String nanosStr = matcher.group(5);
            nanos = Long.parseLong(String.format("%-9s", nanosStr).replace(' ', '0'));
        }

        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusNanos(nanos);
    }
}