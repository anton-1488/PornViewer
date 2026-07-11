package com.plovdev.pornviewer.services.files;

import com.plovdev.pornviewer.core.models.app.AppInfo;
import com.plovdev.pornviewer.services.exceptions.EnvLoadException;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.Properties;

public class EnvReader {
    private final Properties properties = new Properties();

    public EnvReader() {
        this("/.properties");
    }

    public EnvReader(String name) {
        try {
            properties.load(EnvReader.class.getResourceAsStream(name));
        } catch (Exception e) {
            throw new EnvLoadException("Error to load app properties", e);
        }
    }

    public String getEnv(String path) {
        return properties.getProperty(path);
    }

    public String getEnv(String path, String def) {
        return properties.getProperty(path, def);
    }

    @Contract(" -> new")
    public static @NonNull AppInfo loadAppInfo() {
        EnvReader reader = new EnvReader();

        String appName = reader.getEnv("APP_NAME");
        String version = reader.getEnv("VERSION");
        String pvId = reader.getEnv("PV_ID");
        String buildId = reader.getEnv("BUILD_ID");
        LocalDate buildDate = LocalDate.parse(reader.getEnv("BUILD_DATE"), AppInfo.BUILD_DATE_FORMATTER_FROMSTR);
        String vendor = reader.getEnv("VEDNOR");
        String copyright = reader.getEnv("COPYRIGHT");

        return new AppInfo(appName, version, pvId, buildId, buildDate, vendor, copyright);
    }
}