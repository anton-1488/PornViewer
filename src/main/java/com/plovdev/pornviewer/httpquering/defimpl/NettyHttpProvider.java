package com.plovdev.pornviewer.httpquering.defimpl;

import com.plovdev.pornviewer.httpquering.PornRequest;
import com.plovdev.pornviewer.httpquering.PornRequestProvider;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaders;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class NettyHttpProvider implements PornRequestProvider {
    private static final Logger log = LoggerFactory.getLogger(NettyHttpProvider.class);
    private final HttpClient client;

    public NettyHttpProvider() {
        ConnectionProvider provider = ConnectionProvider
                .builder("netty-client")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofMinutes(5))
                .build();

        this.client = HttpClient.create(provider)
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .compress(true)
                .wiretap(true);
    }

    @Override
    public String executeGet(@NonNull PornRequest request) {
        return configureRequest(request).asString().block();
    }

    @Override
    public byte[] executeRaw(@NonNull PornRequest request) {
        return configureRequest(request).asByteArray().block();
    }

    @Override
    public String executePost(@NonNull PornRequest request) {
        return client
                .headers(h -> setupHeaders(h, request))
                .responseTimeout(request.timeout())
                .post()
                .send(ByteBufFlux.fromString(Mono.just(request.body())))
                .uri(request.url())
                .responseContent()
                .aggregate().asString().block();
    }

    @Override
    public InputStream requestStream(PornRequest request) {
        return configureRequest(request).asInputStream().block();
    }

    @Override
    public long checkContentLength(@NonNull PornRequest request) {
        String headerValue = client
                .headers(h -> setupHeaders(h, request))
                .responseTimeout(request.timeout())
                .head()
                .uri(request.url())
                .responseSingle((res, body) -> Mono.just(res.responseHeaders().get("Content-Length")))
                .block();

        try {
            return Long.parseLong(Objects.requireNonNull(headerValue));
        } catch (Exception e) {
            log.error("Error parse content length: ", e);
            return 0;
        }
    }

    private @NonNull ByteBufMono configureRequest(@NonNull PornRequest request) {
        return client
                .headers(h -> setupHeaders(h, request))
                .responseTimeout(request.timeout())
                .get()
                .uri(request.url())
                .responseContent()
                .aggregate();
    }

    private void setupHeaders(HttpHeaders h, @NonNull PornRequest request) {
        Map<String, String> headers = request.headers();
        for (String header : headers.keySet()) {
            h.set(header, headers.get(header));
        }
    }
}