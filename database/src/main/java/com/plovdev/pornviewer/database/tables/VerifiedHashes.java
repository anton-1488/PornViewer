package com.plovdev.pornviewer.database.tables;

import com.plovdev.pornviewer.core.models.app.VerifiedHash;
import com.plovdev.pornviewer.database.SecureDB;
import com.plovdev.pornviewer.database.exceptions.PVDataBaseException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class VerifiedHashes {
    private static final Logger log = LoggerFactory.getLogger(VerifiedHashes.class);
    private static Connection con;

    static {
        con = SecureDB.initCipherer();
        createTable();
    }

    public static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS verified_hashes (
                hash_id TEXT NOT NULL,
                file_type TEXT NOT NULL,
                file_path TEXT NOT NULL,
                file_hash TEXT NOT NULL,
                UNIQUE(hash_id, file_path)
                )""";
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Error to create verified hashes table: ", e);
        }
    }

    public static void dropTable() {
        checkConnection();
        try (Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE verified_hashes");
        } catch (SQLException e) {
            log.error("Error to drop verified hashes table: ", e);
        }
    }

    public static @NonNull List<VerifiedHash> getAllVerifiedHashes() {
        checkConnection();
        List<VerifiedHash> verifiedHashes = new ArrayList<>();

        try (Statement selectAll = con.createStatement();
             ResultSet verifiedHashesSet = selectAll.executeQuery("SELECT * FROM verified_hashes")) {
            while (verifiedHashesSet.next()) {
                verifiedHashes.add(new VerifiedHash(
                        verifiedHashesSet.getString("hash_id"),
                        verifiedHashesSet.getString("file_type"),
                        verifiedHashesSet.getString("file_path"),
                        verifiedHashesSet.getString("file_hash")));
            }
        } catch (SQLException e) {
            log.error("Error to select all verified hashes: ", e);
        }

        return verifiedHashes;
    }

    public static @NonNull List<VerifiedHash> getAllVerifiedHashesByType(VerifiedHash.@NonNull HashedFileType type) {
        checkConnection();
        List<VerifiedHash> verifiedHashes = new ArrayList<>();

        try (PreparedStatement select = con.prepareStatement("SELECT * FROM verified_hashes WHERE file_type = ?")) {
            select.setString(1, type.name());
            try (ResultSet verifiedHashesSet = select.executeQuery()) {
                while (verifiedHashesSet.next()) {
                    verifiedHashes.add(new VerifiedHash(
                            verifiedHashesSet.getString("hash_id"),
                            verifiedHashesSet.getString("file_type"),
                            verifiedHashesSet.getString("file_path"),
                            verifiedHashesSet.getString("file_hash")));
                }
            }
        } catch (SQLException e) {
            log.error("Error to select all verified hashes: ", e);
        }

        return verifiedHashes;
    }

    public synchronized static void addVerifiedHash(@NonNull VerifiedHash hash) {
        checkConnection();

        try (PreparedStatement insert = con.prepareStatement("INSERT OR REPLACE INTO verified_hashes (hash_id, file_type, file_path, file_hash) VALUES (?,?,?,?)")) {
            insert.setString(1, hash.hashId());
            insert.setString(2, hash.fileType().name());
            insert.setString(3, hash.filePath().toString());
            insert.setString(4, hash.hashString());

            insert.executeUpdate();
        } catch (SQLException e) {
            log.error("Error to add verified hash: ", e);
        }
    }

    public synchronized static void removeVerifiedHash(String hashId) {
        checkConnection();

        try (PreparedStatement insert = con.prepareStatement("DELETE FROM verified_hashes WHERE hash_id = ?")) {
            insert.setString(1, hashId);
            insert.executeUpdate();
        } catch (SQLException e) {
            log.error("Error to remove verified hash: ", e);
        }
    }

    public static @NonNull VerifiedHash getVerifiedHash(String hashId) {
        checkConnection();

        try (PreparedStatement select = con.prepareStatement("SELECT * FROM verified_hashes WHERE hash_id = ?")) {
            select.setString(1, hashId);
            try (ResultSet verifiedHashesSet = select.executeQuery()) {
                if (verifiedHashesSet.next()) {
                    return new VerifiedHash(
                            verifiedHashesSet.getString("hash_id"),
                            verifiedHashesSet.getString("file_type"),
                            verifiedHashesSet.getString("file_path"),
                            verifiedHashesSet.getString("file_hash"));
                }
            }
        } catch (SQLException e) {
            log.error("Error to select all verified hashes: ", e);
        }

        throw new NoSuchElementException("Hash " + hashId + " not found in verified hashes.");
    }

    private synchronized static void checkConnection() {
        try {
            if (con.isClosed()) {
                con = SecureDB.initCipherer();
            }
        } catch (SQLException e) {
            log.error("Error re-init connection: ", e);
            throw new PVDataBaseException("Error re-init connection", e, e.getErrorCode());
        }
    }
}