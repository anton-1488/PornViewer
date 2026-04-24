package com.plovdev.pornviewer;

import com.plovdev.pornviewer.databases.FavoriteVideos;
import com.plovdev.pornviewer.databases.SecureDB;
import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFParser;
import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFVideoReader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.models.DownloadedVideoInfo;
import com.plovdev.pornviewer.pornimpl.porn365.DefRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws Exception {
        FavoriteVideos.updateUrls(DefRes.BASE6);
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