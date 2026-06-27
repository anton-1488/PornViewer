package com.plovdev.pornviewer;

import com.plovdev.pornviewer.core.models.app.VerifiedHash;
import com.plovdev.pornviewer.database.tables.VerifiedHashes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger("CLEAR");

    static void main() {
        VerifiedHashes.addVerifiedHash(new VerifiedHash("123", "SYSTEM", "pom.xml", "1234567890123456789012345678901212345678901234567890123456789012"));
        System.out.println(VerifiedHashes.getAllVerifiedHashes());
    }
}