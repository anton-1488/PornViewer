package com.plovdev.pornviewer.database.tables;

import com.plovdev.pornviewer.core.models.adapter.PluginInfo;
import com.plovdev.pornviewer.database.SecureDB;
import com.plovdev.pornviewer.database.exceptions.PVDataBaseException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PluginsProvider {
    private static final Logger log = LoggerFactory.getLogger(PluginsProvider.class);
    private static Connection con;

    private static final String DROP_TABLE = "DROP TABLE plugins";
    private static final String SELECT_ALL = "SELECT * FROM plugins";
    private static final String INSERT_OR_REPLACE = "INSERT OR REPLACE INTO plugins (plugin_id, system_pligin_id, developer_id, title, version, description, author, author_page, license, homepage, image_str, min_app_version, max_app_version, build_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_WHERE_PLUGIN_ID = "DELETE FROM plugins WHERE system_plugin_id = ?";
    private static final String SELECT_WHERE_PLUGIN_ID = "SELECT * FROM plugins WHERE system_plugin_id = ?";

    static {
        con = SecureDB.initCipherer();
        createTable();
    }

    public static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS plugins (
                plugin_id TEXT NOT NULL,
                system_plugin_id TEXT NOT NULL,
                developer_id TEXT NOT NULL,
                title TEXT NOT NULL,
                version TEXT NOT NULL,
                description TEXT NOT NULL,
                author TEXT,
                author_page TEXT,
                license TEXT,
                homepage TEXT,
                image_str TEXT,
                min_app_version INTEGER NOT NULL,
                max_app_version INTEGER NOT NULL,
                build_id INTEGER NOT NULL
                )""";
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Error to create pvva adapters table: ", e);
            throw new PVDataBaseException("Error to create pvva adapters table");
        }
    }

    public static void dropTable() {
        checkConnection();
        try (Statement stt = con.createStatement()) {
            stt.executeUpdate(DROP_TABLE);
        } catch (SQLException e) {
            log.error("Error to drop adapters table: ", e);
            throw new PVDataBaseException("Error to drop adapters table");
        }
    }

    public static @NonNull List<PluginInfo> getAllAdapters() {
        checkConnection();

        List<PluginInfo> adapters = new ArrayList<>();
        try (Statement statement = con.createStatement();
             ResultSet set = statement.executeQuery(SELECT_ALL)) {
            while (set.next()) {
                adapters.add(new PluginInfo(
                        set.getString("plugin_id"),
                        set.getString("system_plugin_id"),
                        set.getString("developer_id"),
                        set.getString("title"),
                        set.getString("version"),
                        set.getString("description"),
                        set.getString("author"),
                        set.getString("author_page") != null ? URI.create(set.getString("author_page")) : null,
                        set.getString("license") != null ? URI.create(set.getString("license")) : null,
                        set.getString("homepage") != null ? URI.create(set.getString("homepage")) : null,
                        set.getString("image_str"),
                        set.getInt("min_app_version"),
                        set.getInt("max_app_version"),
                        set.getInt("build_id")
                ));
            }
        } catch (SQLException e) {
            log.error("Error to select all adapters: ", e);
        }
        return adapters;
    }

    public synchronized static void addAdapter(@NonNull PluginInfo info) {
        checkConnection();
        try (PreparedStatement stt = con.prepareStatement(INSERT_OR_REPLACE)) {
            stt.setString(1, info.pluginId());
            stt.setString(2, info.systemPluginId());
            stt.setString(3, info.developerId());
            stt.setString(4, info.title());
            stt.setString(5, info.version());
            stt.setString(6, info.description());
            stt.setString(7, info.author());
            stt.setObject(8, info.authorPage());
            stt.setObject(9, info.license());
            stt.setObject(10, info.homepage());
            stt.setString(11, info.imageString());
            stt.setInt(12, info.minAppVersion());
            stt.setInt(13, info.maxAppVersion());
            stt.setInt(14, info.buildId());
            stt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error to add adapter: {} - {}", info.pluginId(), e.getMessage());
        }
    }

    public synchronized static void removeAdapter(String systemPluginId) {
        checkConnection();

        try (PreparedStatement stt = con.prepareStatement(DELETE_WHERE_PLUGIN_ID)) {
            stt.setString(1, systemPluginId);
            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to remove adapter: ", e);
        }
    }

    public static @NonNull PluginInfo getAdapterById(String systemPluginId) {
        checkConnection();

        try (PreparedStatement stt = con.prepareStatement(SELECT_WHERE_PLUGIN_ID)) {
            stt.setString(1, systemPluginId);
            try (ResultSet set = stt.executeQuery()) {
                if (set.next()) {
                    return new PluginInfo(
                            set.getString("plugin_id"),
                            set.getString("system_plugin_id"),
                            set.getString("developer_id"),
                            set.getString("title"),
                            set.getString("version"),
                            set.getString("description"),
                            set.getString("author"),
                            set.getString("author_page") != null ? URI.create(set.getString("authorPage")) : null,
                            set.getString("license") != null ? URI.create(set.getString("license")) : null,
                            set.getString("homepage") != null ? URI.create(set.getString("homepage")) : null,
                            set.getString("image_str"),
                            set.getInt("min_app_version"),
                            set.getInt("max_app_version"),
                            set.getInt("build_id")
                    );
                }
            }
        } catch (SQLException e) {
            log.error("Error to search adapter by id: ", e);
        }

        throw new NoSuchElementException("Adapter with system pluin id " + systemPluginId + " not found.");
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