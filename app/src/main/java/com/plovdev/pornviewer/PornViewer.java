package com.plovdev.pornviewer;

import com.plovdev.pornviewer.core.http.PornHttpClient;
import com.plovdev.pornviewer.core.http.PornRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger(PornViewer.class);

    static void main(String[] args) {
        PornHttpClient client = new PornHttpClient();
        System.out.println(client.executeString(PornRequest.get("https://www.yaeby.pro/")));
    }
}