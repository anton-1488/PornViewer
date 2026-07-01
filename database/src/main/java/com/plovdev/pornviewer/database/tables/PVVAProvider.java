package com.plovdev.pornviewer.database.tables;

import com.plovdev.pornviewer.core.models.adapter.PluginInfo;
import com.plovdev.pornviewer.core.models.adapter.PluginsListItem;
import com.plovdev.pornviewer.database.SecureDB;
import com.plovdev.pornviewer.database.exceptions.PVDataBaseException;
import com.plovdev.pornviewer.core.exceptions.PornViewerException;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.read.DefaultPVVAReader;
import org.plovdev.pvva.read.PVVAReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PVVAProvider {
    private static final Logger log = LoggerFactory.getLogger(PVVAProvider.class);
    private static Connection con;

    static {
        con = SecureDB.initCipherer();
        createTable();
    }

    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS pvva_adapters (" +
                "pluginId TEXT, " +
                "minAppVersion INTEGER, " +
                "maxAppVersion INTEGER, " +
                "name TEXT, " +
                "version TEXT, " +
                "description TEXT, " +
                "updateUrl TEXT, " +
                "signRequired INTEGER, " +
                "author TEXT, " +
                "authorPage TEXT, " +
                "licenseUrl TEXT, " +
                "homePage TEXT, " +
                "pathName TEXT" +
                ")";
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Error to create pvva adapters table: ", e);
        }
    }

    public static void dropTable() {
        checkConnection();
        try (Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE pvva_adapters");
        } catch (SQLException e) {
            log.error("Error to drop adapters table: ", e);
        }
    }

    public static @NonNull List<PluginsListItem> getAllAdapters() {
        checkConnection();

        List<PluginsListItem> adapters = new ArrayList<>();
        try (Statement statement = con.createStatement();
             ResultSet set = statement.executeQuery("SELECT * FROM pvva_adapters")) {
            while (set.next()) {
                adapters.add(null);
            }
        } catch (SQLException e) {
            log.error("Error to select all adapters: ", e);
        }
        return adapters;
    }

    public synchronized static void addAdapter(@NonNull PluginsListItem info) {
        checkConnection();
        String sql = "INSERT OR REPLACE INTO pvva_adapters (" +
                "pluginId, minAppVersion, maxAppVersion, name, version, description, " +
                "updateUrl, signRequired, author, authorPage, licenseUrl, homePage, pathName" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stt = con.prepareStatement(sql)) {
//            stt.setString(1, info.pluginId());
//            stt.setInt(2, info.minVers());
//            stt.setInt(3, info.maxVers());
//            stt.setString(4, info.name());
//            stt.setString(5, info.version());
//            stt.setString(6, info.descr());
//            stt.setString(7, info.updateUrl());
//            stt.setBoolean(8, info.signRequeired());
//            stt.setString(9, info.author());
//            stt.setString(10, info.authorPage());
//            stt.setString(11, info.licenseUrl());
//            stt.setString(12, info.homepage());
//            stt.setString(13, info.pathName());
            stt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error to add adapter: {} - {}", info.pluginId(), e.getMessage());
        }
    }

    public synchronized static void removeAdapter(String pluginId) {
        checkConnection();

        try (PreparedStatement stt = con.prepareStatement("DELETE FROM pvva_adapters WHERE pluginId = ?")) {
            stt.setString(1, pluginId);
            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to remove adapter: ", e);
        }
    }

    public static @NonNull PluginsListItem getAdapterById(String pluginId) {
        checkConnection();

        try (PreparedStatement stt = con.prepareStatement("SELECT * FROM pvva_adapters WHERE pluginId = ?")) {
            stt.setString(1, pluginId);
            try (ResultSet set = stt.executeQuery()) {
                if (set.next()) {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Error to search adapter by id: ", e);
        }
        throw new NoSuchElementException("Adapter with id(" + pluginId + ") not found.");
    }

    public static @NonNull PluginsListItem loadAdapter(Path path) {
        try (PVVAReader reader = new DefaultPVVAReader(path)) {
            PVVAHost host = reader.readVideoAdapter();
            PVVAHeader header = host.header();
            PluginJson pluginJson = host.pluginJson();

            PluginsListItem info = null;
            addAdapter(info);
            return info;
        } catch (Exception e) {
            throw new PornViewerException("Error to load adapter: ", e);
        }
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