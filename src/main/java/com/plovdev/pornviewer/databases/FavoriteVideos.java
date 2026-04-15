package com.plovdev.pornviewer.databases;

import com.plovdev.pornviewer.models.FavoriteVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FavoriteVideos {
    private static final Logger log = LoggerFactory.getLogger(FavoriteVideos.class);
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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Favorites (id TEXT, url TEXT, title TEXT, pic TEXT, duration TEXT, views TEXT, rating TEXT, mark TEXT)");
        } catch (Exception e) {
            log.error("Error to create favorites table: ", e);
        }
    }

    public static void dropTable() {
        try (Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE Favorites");
        } catch (Exception e) {
            log.error("Error to drop favorites table: ", e);
        }
    }

    public static void add(FavoriteVideo card) {
        try (PreparedStatement stt = con.prepareStatement("INSERT INTO Favorites (id, url, title, pic, duration, views, rating, mark) VALUES (?,?,?,?,?,?,?,?)")) {
            stt.setString(1, String.valueOf(card.getCardId()));
            stt.setString(2, card.getUrl().trim());
            stt.setString(3, card.getTitle().trim());
            stt.setString(4, card.getPic().trim());
            stt.setString(5, card.getDuration().trim());
            stt.setString(6, String.valueOf(card.getViews()));
            stt.setString(7, card.getRating().trim());
            stt.setString(8, card.getGroup());

            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error to add video to favorite: ", e);
        }
    }

    public static FavoriteVideo get(String id) {
        try (PreparedStatement stt = con.prepareStatement("SELECT * FROM Favorites WHERE id = ?")) {
            stt.setString(1, id);

            try (ResultSet set = stt.executeQuery()) {
                if (set.next()) {
                    int cardId = Integer.parseInt(id);
                    String title = set.getString("title");
                    String url = set.getString("url");
                    String pic = set.getString("pic");
                    String duration = set.getString("duration");
                    int views = Integer.parseInt(set.getString("views"));
                    String rating = set.getString("rating");
                    String group = set.getString("mark");

                    return new FavoriteVideo(cardId, title, url, pic, duration, views, rating, null, true, group);
                }
            }
        } catch (Exception e) {
            log.error("Error get favorite video: ", e);
        }
        return null;
    }

    public static List<FavoriteVideo> getAll() {
        List<FavoriteVideo> list = new CopyOnWriteArrayList<>();
        try {
            Statement stt = con.createStatement();
            ResultSet set = stt.executeQuery("SELECT * FROM Favorites");

            while (set.next()) {
                int cardId = Integer.parseInt(set.getString("id"));
                String title = set.getString("title");
                String url = set.getString("url");
                String pic = set.getString("pic");
                String duration = set.getString("duration");
                int views = Integer.parseInt(set.getString("views"));
                String rating = set.getString("rating");
                String group = set.getString("mark");

                list.add(new FavoriteVideo(cardId, title, url, pic, duration, views, rating, null, true, group));
            }
        } catch (Exception e) {
            log.error("Error to get all favorite videos: ", e);
        }
        return list.reversed();
    }

    public static List<Integer> getAllId() {
        List<Integer> list = new ArrayList<>();
        try (Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT id FROM Favorites")) {

            while (set.next()) {
                list.add(Integer.parseInt(set.getString("id")));
            }
        } catch (Exception e) {
            log.error("Error to get all favorite video IDs: ", e);
        }
        return list;
    }

    public static List<FavoriteVideo> getAll(String group) {
        List<FavoriteVideo> list = new CopyOnWriteArrayList<>();
        try (PreparedStatement stt = con.prepareStatement("SELECT * FROM Favorites WHERE mark = ?")) {
            stt.setString(1, group);
            try (ResultSet set = stt.executeQuery()) {
                while (set.next()) {
                    int cardId = Integer.parseInt(set.getString("id"));
                    String title = set.getString("title");
                    String url = set.getString("url");
                    String pic = set.getString("pic");
                    String duration = set.getString("duration");
                    int views = Integer.parseInt(set.getString("views"));
                    String rating = set.getString("rating");

                    list.add(new FavoriteVideo(cardId, title, url, pic, duration, views, rating, null, true, group));
                }
            }
        } catch (Exception e) {
            log.error("Error to get all favorite videos: ", e);
        }
        return list;
    }

    public static List<Integer> getAllId(String group) {
        List<Integer> list = new ArrayList<>();
        try (Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT id FROM Favorites WHERE mark = '" + group + "'")) {

            while (set.next()) {
                list.add(Integer.parseInt(set.getString("id")));
            }
        } catch (Exception e) {
            log.error("Error to get all favorite video IDs: ", e);
        }
        return list;
    }

    public static void update(String key, String val, int id) {
        try (PreparedStatement stt = con.prepareStatement("UPDATE Favorites SET " + key + " = ? WHERE id = ?")) {

            stt.setString(1, val);
            stt.setString(2, String.valueOf(id));

            stt.executeUpdate();
        } catch (Exception e) {
            log.error("Error update {} to {} in favorite video {}: ", key, val, id, e);
        }
    }

    public static void remove(String value) {
        try (Statement stat = con.createStatement()) {

            stat.executeUpdate("DELETE FROM Favorites WHERE id = " + value);
        } catch (Exception e) {
            log.error("Error to remove favorite video: ", e);
        }
    }

    public static void updateUrls(String url2) {
        for (FavoriteVideo card : FavoriteVideos.getAll()) {
            log.info("Updating card: {}", card);
            FavoriteVideos.update("url", url2 + "/movie/" + card.getCardId(), card.getCardId());
        }
    }
}