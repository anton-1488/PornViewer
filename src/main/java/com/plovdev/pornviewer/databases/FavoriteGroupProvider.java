package com.plovdev.pornviewer.databases;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FavoriteGroupProvider {
    private static final Logger log = LoggerFactory.getLogger(FavoriteGroupProvider.class);
    private static final Connection con;

    static {
        try {
            con = SecureDB.initCipherer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        createTable();
    }

    public static void createTable() {
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS FavoriteGroups (name TEXT)");
        } catch (Exception e) {
            log.error("Error to create favorite groups table: ", e);
        }
    }

    public static void dropTable() {
        try (Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE FavoriteGroups");
        } catch (Exception e) {
            log.error("Error to drop favorite groups table: ", e);
        }
    }

    public static @NonNull List<String> getAllGroups() {
        List<String> groups = new CopyOnWriteArrayList<>();
        try (Statement statement = con.createStatement();
             ResultSet set = statement.executeQuery("SELECT * FROM FavoriteGroups")) {
            while (set.next()) {
                groups.add(set.getString("name"));
            }
        } catch (Exception e) {
            log.error("Error to select all favorite groups: ", e);
        }
        return groups;
    }

    public static void addGroup(String name) {
        try (PreparedStatement stt = con.prepareStatement("INSERT INTO FavoriteGroups (name) VALUES (?)")) {
            stt.setString(1, name);
            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to add group to favorite groups: ", e);
        }
    }

    public static void removeGroup(String name) {
        try (PreparedStatement stt = con.prepareStatement("DELETE FROM FavoriteGroups WHERE name = ?")) {
            stt.setString(1, name);
            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to remove group from favorite groups: ", e);
        }
    }
}