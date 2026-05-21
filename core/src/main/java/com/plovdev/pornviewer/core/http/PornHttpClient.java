package com.plovdev.pornviewer.core.http;

import com.plovdev.pornviewer.commons.models.porn.CategoryInfo;
import com.plovdev.pornviewer.commons.models.porn.FullVideoInfo;
import com.plovdev.pornviewer.commons.models.porn.ModelInfo;
import com.plovdev.pornviewer.commons.models.porn.ShortVideoInfo;
import com.plovdev.pornviewer.core.http.providers.HttpClientRequestProvider;
import com.plovdev.pornviewer.core.http.providers.OkHttpRequestProvider;
import com.plovdev.pornviewer.core.http.providers.PornRequestProvider;
import com.plovdev.pornviewer.database.UserSettingsManager;
import com.plovdev.pornviewer.exceptions.NoSuchRequestProviderException;
import com.plovdev.pornviewer.pvvasupport.PVVASupportManager;
import com.plovdev.pornviewer.pvvasupport.parser.ScriptEngineExecutor;
import com.plovdev.pornviewer.commons.events.GlobalEventManager;
import com.plovdev.pornviewer.commons.events.VideoDownloadingChannel;
import com.plovdev.pornviewer.utils.http.UriBuilder;
import com.plovdev.pornviewer.utils.json.DownloadedVideoInfo;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.configs.httpconfig.HeadersConfig;
import org.plovdev.pvva.models.configs.httpconfig.HttpClientType;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.plovdev.pvva.models.configs.resourceconfig.CategoriesResources;
import org.plovdev.pvva.models.configs.resourceconfig.MainResources;
import org.plovdev.pvva.models.configs.resourceconfig.ModelsResources;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.utils.PVVAResourceChecker;
import org.plovdev.pvva.utils.vars.Variable;
import org.plovdev.pvva.utils.vars.VariableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class PornHttpClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PornHttpClient.class);
    private static final HttpConfig DEFAULT_HTTP_CONFIG = new HttpConfig(HttpClientType.OK_HTTP_CLIENT, new HeadersConfig(false, List.of(PornRequest.getDefaultHeaders())), RetryPolicy.ON_FAILED, 0, 0, 0, 3, 1000);
    private static final Charset DEFAULT = StandardCharsets.UTF_8;
    private final PornRequestProvider requestProvider;
    private final PVVAHost host;
    private final ResourceConfig resourceConfig;
    private final PVVAResourceChecker checker;
    private final ScriptEngineExecutor scriptEngine;
    private volatile String baseUrl;

    public PornHttpClient() {
        PVVAHost host = Objects.requireNonNull(PVVASupportManager.loadPvvaById(UserSettingsManager.getUserSettings().adapter()));
        this(host);
    }

    public PornHttpClient(@NonNull PVVAHost host) {
        requestProvider = createProvider(host.optHttpConfig().orElse(DEFAULT_HTTP_CONFIG));
        this.host = host;
        this.resourceConfig = host.resourceConfig();
        this.checker = new PVVAResourceChecker(resourceConfig);
        this.baseUrl = resourceConfig.baseUrl();
        this.scriptEngine = new ScriptEngineExecutor(host.mainParser());
    }

    private @Nullable PornRequestProvider createProvider(@NonNull HttpConfig config) {
        HttpClientType type = config.httpClient();
        return switch (type) {
            case JAVA_HTTP_CLIENT -> new HttpClientRequestProvider(config);
            case APACHE_HTTP_CLIENT -> null; // TODO: Add Apache http request provider
            case OK_HTTP_CLIENT -> new OkHttpRequestProvider(config);
            case NETTY -> null; // TODO: Add Netty http request provider
            default -> throw new NoSuchRequestProviderException("No request provider: " + type.name(), type);
        };
    }

    public List<ShortVideoInfo> requestMainPage(int page) {
        UriBuilder builder = new UriBuilder(baseUrl);
        MainResources resources = resourceConfig.mainResources();
        if (checker.supportMain()) {
            String endpoint = VariableHandler.processVariables(resources.endpoint().get(), Map.of(Variable.PAGE, String.valueOf(page)));
            builder.appendUriPart(endpoint);
            String response = requestProvider.executeGet(PornRequest.get(builder.build()));
            System.out.println(response);
            return scriptEngine.parseVideos(response);
        } else {
            throw new UnsupportedOperationException("This adapter not support main page");
        }
    }

    public List<ShortVideoInfo> searchMainPage(String rawSearch, int page) {
        UriBuilder builder = new UriBuilder(baseUrl);
        MainResources resources = resourceConfig.mainResources();
        if (checker.supportMainSearch()) {
            String preparedSearch = URLEncoder.encode(rawSearch, DEFAULT);
            String searchUrl = VariableHandler.processVariables(resources.searchUrl().get(), Map.of(Variable.USER_INPUT, preparedSearch, Variable.PAGE, String.valueOf(page)));
            builder.appendUriPart(searchUrl);
            return scriptEngine.parseVideos(requestProvider.executeGet(PornRequest.get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support search main page");
        }
    }

    public List<ModelInfo> requestModelsPage(int page) {
        UriBuilder builder = new UriBuilder(baseUrl);
        ModelsResources resources = resourceConfig.modelsResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support models page"));
        if (checker.supportModels()) {
            String endpoint = VariableHandler.processVariables(resources.endpoint().get(), Map.of(Variable.PAGE, String.valueOf(page)));
            builder.appendUriPart(endpoint);
            return scriptEngine.parseModels(requestProvider.executeGet(PornRequest.get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support models page");
        }
    }

    public List<ShortVideoInfo> requestModelPage(String modelName) {
        UriBuilder builder = new UriBuilder(baseUrl);
        ModelsResources resources = resourceConfig.modelsResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support model page"));
        if (checker.supportModel()) {
            String modelUrl = VariableHandler.processVariables(resources.modelEndpoint().get(), Map.of(Variable.MODEL_NAME, modelName));
            builder.appendUriPart(modelUrl);
            return scriptEngine.parseVideos(requestProvider.executeGet(PornRequest.get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support model page");
        }
    }

    public List<ModelInfo> searchModelsPage(String rawSearch) {
        UriBuilder builder = new UriBuilder(baseUrl);
        ModelsResources resources = resourceConfig.modelsResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support search models page"));
        if (checker.supportModelsSearch()) {
            String preparedSearch = URLEncoder.encode(rawSearch, DEFAULT);
            String searchUrl = VariableHandler.processVariables(resources.modelSearchEndpoint().get(), Map.of(Variable.USER_INPUT, preparedSearch));
            builder.appendUriPart(searchUrl);
            return scriptEngine.parseModels(requestProvider.executeGet(PornRequest.get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support search models page");
        }
    }

    public List<CategoryInfo> requestCategories() {
        UriBuilder builder = new UriBuilder(baseUrl);
        CategoriesResources resources = resourceConfig.categoriesResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support categories page"));
        if (checker.supportCategories()) {
            String endpoint = resources.endpoint().get();
            builder.appendUriPart(endpoint);
            return scriptEngine.parseCategories(requestProvider.executeGet(PornRequest.get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support categories page");
        }
    }

    public List<ShortVideoInfo> requestCategoryPage(String categoryName) {
        UriBuilder builder = new UriBuilder(baseUrl);
        CategoriesResources resources = resourceConfig.categoriesResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support category page"));
        if (resources.supports() && resources.endpoint().isPresent()) {
            if (checker.supportCategory()) {
                String categoryUrl = VariableHandler.processVariables(resources.categoryEndpoint().get(), Map.of(Variable.CATEGORY, categoryName));
                builder.appendUriPart(categoryUrl);
                return scriptEngine.parseVideos(requestProvider.executeGet(PornRequest.get(builder.build())));
            } else {
                throw new UnsupportedOperationException("This adapter not support category page");
            }
        } else {
            throw new UnsupportedOperationException("This adapter not support category page");
        }
    }

    public FullVideoInfo requestVideoPage(String videoId) {
        UriBuilder builder = new UriBuilder(baseUrl);
        if (checker.supportVideo()) {
            String videoUrl = VariableHandler.processVariables(resourceConfig.videoEndpoint(), Map.of(Variable.VIDEO_ID, videoId));
            builder.appendUriPart(videoUrl);
            return scriptEngine.parseFullVideoInfo(requestProvider.executeGet(PornRequest.get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support video page");
        }
    }

    @Contract("_ -> new")
    public @NonNull ByteArrayInputStream requestBytesData(PornRequest request) {
        return new ByteArrayInputStream(requestProvider.executeRaw(request));
    }

    public @Nullable CompletableFuture<DownloadedVideoInfo> startDownload(PornRequest request, FullVideoInfo info, @NonNull String output) {
        log.info("Start loading to file: {}", output);
        long videoSize = requestProvider.checkContentLength(request);
        GlobalEventManager.broadcastEvent(new VideoDownloadingChannel(videoSize, VideoDownloadingChannel.DownloadedType.START));

        PornDownloader downloader = new PornDownloader(requestProvider, request, output);
        return downloader.startDownload(videoSize, info, requestProvider.executeRaw(PornRequest.get(info.previewUrl())));
    }

    //======================================\\

    public synchronized void setProxy(Proxy proxy) {
        requestProvider.setProxy(proxy);
    }

    public synchronized void setMirror(int index) {
        if (checker.supportMirrors()) {
            List<String> mirrors = resourceConfig.mirrors().get();
            if (!mirrors.isEmpty()) {
                this.baseUrl = mirrors.get(index); // Thorws index exception if index is outside
            }
        } else {
            throw new UnsupportedOperationException("This adapter not support mirrors");
        }
    }

    public PornRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public PVVAHost getHost() {
        return host;
    }

    @Override
    public synchronized void close() {
        requestProvider.close();
    }
}