package com.plovdev.pornviewer;

import com.plovdev.pornviewer.databases.SecureDB;
import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFParser;
import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFVideoReader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.DownloadedVideoInfo;
import com.plovdev.pornviewer.models.VideoCard;
import com.plovdev.pornviewer.pornimpl.porn365.DefPornParser;
import com.plovdev.pornviewer.pornimpl.porn365.DefRes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    //Мачехе лижу манду, это не айс в моем кругу
    public static void main(String[] args) throws Exception {
        PBPornHandler handler = new PBPornHandler();
        DefPornParser pornParser = new DefPornParser();
        DefRes resourcer = new DefRes();
        String searchStr = URLEncoder.encode("После хорошего секса поменяла свое решение", Charset.defaultCharset());
        String url = resourcer.baseUrl() + resourcer.searchUrl() + searchStr + "/popular/";
        searchVideo(pornParser, handler, url, List.of("хотела", "секса", "поменяла", "решение"));
    }

    private static void searchVideo(@NotNull DefPornParser pornParser, @NotNull PBPornHandler handler, String url, List<String> keywords) {
        for (int i = 0; i < 300; i++) {
            try {
                log.info("========DEBUG=process card: {}========", i);
                Optional<VideoCard> maybeCard = containsKeywordsInVideos(pornParser.getAllVideos(handler.requestPorn(url + i)), keywords);
                maybeCard.ifPresent(mc -> log.info("Found card: {}", mc));
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Search error: ", e);
            }
        }
    }

    @Contract(pure = true)
    private static @NotNull Optional<VideoCard> containsKeywordsInVideos(@NotNull List<VideoCard> videoCards, @NotNull List<String> keywords) {
        return videoCards.stream()
                .filter(card -> keywords.stream().allMatch(keyword -> card.getTitle().toLowerCase().contains(keyword)))
                .findFirst();
    }

    private static void processVideo(String videoFile) {
        File file = new File("/Users/mac/.PornViewer/downloads/" + videoFile);
        try (PVVFParser parser = new PVVFParser(file)) {
            EncryptedVideo video = parser.collectEncryptedVideo();
            System.out.println(video);
            DownloadedVideoInfo info = PVVFVideoReader.readInfo(file);
            System.out.println(info);
        } catch (Exception e) {
            log.error("Reading error: ", e);
        }
    }

    public static void encryptDatabase(String newPassword) {
        try {
            Connection conn = SecureDB.initCipherer();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA rekey = '" + newPassword + "'");
                System.out.println("✅ База успешно зашифрована");
            }
            conn.close();
        } catch (Exception e) {
            System.err.println("❌ Ошибка при шифровании: " + e.getMessage());
        }
    }
}