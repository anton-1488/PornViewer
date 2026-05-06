package com.plovdev.pornviewer.databases;

import com.plovdev.pornviewer.encryptionsupport.CipherEngineUtils;
import com.plovdev.pornviewer.utility.files.FileUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class SecureDB {
    private static final Logger log = LoggerFactory.getLogger(SecureDB.class);

    public static void initDB() {
        try (Connection conn = initCipherer();
             Statement st = conn.createStatement()) {
            st.execute("SELECT count(*) FROM sqlite_master");
        } catch (Exception e) {
            log.error("Error to init db: ", e);
        }
    }

    public static synchronized @NonNull Connection initCipherer() {
        try {
            FileUtils fileUtils = new FileUtils();

            Class.forName("org.sqlite.JDBC");
            String password = CipherEngineUtils.getPassword();
            String url = fileUtils.getPVJDBCPathProtocol();
            Properties props = new Properties();
            props.setProperty("cipher", "chacha20");
            props.setProperty("key", password);
            props.setProperty("temp_store", "MEMORY");

            Connection connection = DriverManager.getConnection(url, props);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
            }

            return connection;
        } catch (Exception e) {
            log.error("Failed to initialize encrypted database", e);
            throw new RuntimeException(e);
        }
    }
}