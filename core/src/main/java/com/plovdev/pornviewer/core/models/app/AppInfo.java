package com.plovdev.pornviewer.core.models.app;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record AppInfo(String appName,
                      String version,
                      String pvId,
                      String buildId,
                      LocalDate buildDate,
                      String vendor,
                      String copyright) {
    public static final DateTimeFormatter BUILD_DATE_FORMATTER_TOSTR = DateTimeFormatter.ofPattern("dd, MMMM, yyyy");
    public static final DateTimeFormatter BUILD_DATE_FORMATTER_FROMSTR = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Override
    public @NonNull String toString() {
        return String.format("""
                ==============================================
                  %s-%s(%s)
                  %s. Built at %s
                
                  Powered by %s
                  Copyright: %s
                ==============================================
                """, appName, version, pvId, buildId, buildDate.format(BUILD_DATE_FORMATTER_TOSTR), vendor, copyright);
    }
}