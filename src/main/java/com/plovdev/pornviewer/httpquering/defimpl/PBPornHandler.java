package com.plovdev.pornviewer.httpquering.defimpl;

import com.plovdev.pornviewer.events.listeners.EventListener;
import com.plovdev.pornviewer.httpquering.PornRequest;
import com.plovdev.pornviewer.httpquering.PornRequestProvider;
import com.plovdev.pornviewer.httpquering.RequestProvider;
import com.plovdev.pornviewer.models.VideoInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class PBPornHandler {
    private static final Logger log = LoggerFactory.getLogger(PBPornHandler.class);

    private PornRequestProvider requestProvider;

    public PBPornHandler(RequestProvider provider) {
        if (provider == RequestProvider.OK_HTTP) {
            requestProvider = new OkHttpRequestProvider();
        } else {
            requestProvider = new HttpClientRequstProvider();
        }
    }

    public PBPornHandler() {
        requestProvider = new HttpClientRequstProvider();
    }

    public PornRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public void setRequestProvider(PornRequestProvider requestProvider) {
        this.requestProvider = requestProvider;
    }

    public String requestPorn(String url) {
        return requestProvider.executeGet(PornRequest.get(url));
    }

    public byte[] getBytes(String url) {
        return requestProvider.executeRaw(PornRequest.get(url));
    }

    public String executePost(String url, String body) {
        return requestProvider.executePost(PornRequest.post(url, body));
    }

    public void downloadPorn(String url, String filename, VideoInfo info) {
        log.info("Start loading file: {}", filename);
        EventListener.notifyListeners("START_DWONLOAD:" + filename);
        long videoSize = requestProvider.checkContentLength(PornRequest.get(url));

        PornDownloader downloader = new PornDownloader(requestProvider, URI.create(url), filename);
        downloader.startDownload(videoSize, info, requestProvider.executeRaw(PornRequest.get(info.getPic())));
    }

    public long getVideoSize(String url) {
        return requestProvider.checkContentLength(PornRequest.get(url));
    }

    public String getNextLink(String html) {
        try {
            Document document = Jsoup.parse(html);
            Elements elements = document.select("div.navigation");

            return elements.stream().findFirst().orElseThrow().attr("abs:href");
        } catch (Exception e) {
            return null;
        }
    }
}
