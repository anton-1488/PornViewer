package com.plovdev.pornviewer.databases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserPreferences {
    private static final Connection con;
    private static final Logger log = LoggerFactory.getLogger(UserPreferences.class);

    static {
        try {
            con = SecureDB.initCipherer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        createTable();
    }

    public synchronized static void createTable() {
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Prefs (id TEXT, pvva TEXT)");
            statement.executeUpdate("INSERT OR IGNORE INTO Prefs VALUES ('0000', 'p365')");
        } catch (Exception e) {
            log.error("Error to create user prefs: ", e);
        }
    }

    public static void dropTable() {
        try (Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE Prefs");
        } catch (Exception e) {
            log.error("Error to drop user prefs: ", e);
        }
    }

    public static void add(User user) {
        try (PreparedStatement stt = con.prepareStatement("INSERT INTO Prefs (id, pvva) VALUES (?,?)")) {
            stt.setString(1, user.getId());
            stt.setString(2, user.getPvva());

            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to add new user: ", e);
        }
    }

    public static User get(String id) {
        try (PreparedStatement stt = con.prepareStatement("SELECT * FROM Prefs WHERE id = ?")) {
            stt.setString(1, id);
            try (ResultSet set = stt.executeQuery()) {
                if (set.next()) {
                    return new User(set.getString("pvva"), id);
                }
            }
        } catch (Exception e) {
            log.error("Error to get user: ", e);
        }
        return new User("porno365", "0000");
    }

    public static List<User> getAll() {
        List<User> list = new ArrayList<>();
        try (Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Prefs")) {

            while (set.next()) {
                User ret = new User();
                ret.setId(set.getString("id"));
                ret.setPvva(set.getString("pvva"));
                list.add(ret);
            }
        } catch (Exception e) {
            log.error("Error to get all users: ", e);
        }
        return list;
    }

    public static void update(String key, String val, String id) {
        try (PreparedStatement stt = con.prepareStatement("UPDATE Prefs SET " + key + " = ? WHERE id = ?")) {

            stt.setString(1, val.trim());
            stt.setString(2, id.trim());

            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to update user({}) key {} to {}: ", key, id, val, e);
        }
    }

    public static void remove(String id) {
        try (PreparedStatement stt = con.prepareStatement("DELETE FROM Prefs WHERE id = ?")) {
            stt.setString(1, id);
            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to remove user {} :", id, e);
        }
    }
}