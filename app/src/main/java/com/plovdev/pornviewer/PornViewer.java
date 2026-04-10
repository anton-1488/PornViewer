package com.plovdev.pornviewer;

import com.plovdev.pornviewer.core.http.PornHttpClient;
import com.plovdev.pornviewer.core.http.PornRequest;

public class PornViewer {
    public static void main(String[] args) {
        PornHttpClient client = PornHttpClient.getInstance();
        String body = client.executeString(PornRequest.get("http://6porno365.info"));
        System.out.println(body);
    }
}