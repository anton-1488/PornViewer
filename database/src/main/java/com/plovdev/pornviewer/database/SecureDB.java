package com.plovdev.pornviewer.database;

import com.plovdev.pornviewer.core.exceptions.NotFoundException;
import com.plovdev.pornviewer.database.exceptions.PVDataBaseException;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.services.files.PVFileManager;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

public class SecureDB {
    private static final Logger log = LoggerFactory.getLogger(SecureDB.class);

    public static void initDB() {
        try (Connection conn = initCipherer();
             Statement st = conn.createStatement()) {
            st.execute("SELECT count(*) FROM sqlite_master");
        } catch (SQLException e) {
            log.error("Error to execute initiliaze request to db: ", e);
        }
    }

    public static synchronized @NonNull Connection initCipherer() {
        byte[] password = PVSecurityManager.getPassword();
        Properties props = new Properties();
        try {
            Class.forName("org.sqlite.JDBC");
            String url = PVFileManager.getPVJDBCPathProtocol();
            props.setProperty("cipher", "chacha20");
            props.setProperty("key", new String(password));
            props.setProperty("temp_store", "MEMORY");

            Connection connection = DriverManager.getConnection(url, props);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new NotFoundException("org.sqlite.JDBC not found.", e);
        } catch (SQLException e) {
            log.error("Failed to initialize encrypted database", e);
            throw new PVDataBaseException("Error init encrypted db connection: " + e.getMessage(), e, e.getErrorCode());
        } finally {
            Arrays.fill(password, (byte) 0);
            props.remove("key");
        }
    }
}