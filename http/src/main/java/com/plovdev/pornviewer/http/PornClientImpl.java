package com.plovdev.pornviewer.http;

import com.plovdev.pornviewer.core.events.DownloadingType;
import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.VideoDownloadingChannel;
import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.models.porn.CategoryInfo;
import com.plovdev.pornviewer.core.models.porn.FullVideoInfo;
import com.plovdev.pornviewer.core.models.porn.ModelInfo;
import com.plovdev.pornviewer.core.models.porn.ShortVideoInfo;
import com.plovdev.pornviewer.database.tables.UserSettingsManager;
import com.plovdev.pornviewer.http.providers.HttpClientRequestProvider;
import com.plovdev.pornviewer.http.providers.OkHttpRequestProvider;
import com.plovdev.pornviewer.http.providers.PornRequestProvider;
import com.plovdev.pornviewer.pvvasupport.exceptions.NoSuchRequestProviderException;
import com.plovdev.pornviewer.pvvasupport.loading.PVVALoaderManager;
import com.plovdev.pornviewer.pvvasupport.parser.ScriptEngineExecutor;
import com.plovdev.pornviewer.services.http.UriBuilder;
import com.plovdev.pornviewer.services.json.DownloadedVideoInfo;
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

import java.net.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.plovdev.pornviewer.core.http.PornRequest.get;
import static com.plovdev.pornviewer.core.http.PornRequest.head;
import static com.plovdev.pornviewer.core.utils.Globals.DEFAULT_CHARSET;

public final class PornClientImpl implements PornClient {
    private static final Logger log = LoggerFactory.getLogger(PornClientImpl.class);
    private static final HttpConfig DEFAULT_HTTP_CONFIG = new HttpConfig(HttpClientType.OK_HTTP_CLIENT, new HeadersConfig(false, List.of(PornRequest.getDefaultHeaders())), RetryPolicy.ON_FAILED, 1000, 1000, 1000, 1000, 3);

    private final PornRequestProvider requestProvider;
    private final PVVAHost host;
    private final ResourceConfig resourceConfig;
    private final PVVAResourceChecker checker;
    private final ScriptEngineExecutor scriptEngine;
    private final AtomicReference<String> baseUrl;

    public PornClientImpl() {
        PVVAHost host = Objects.requireNonNull(PVVALoaderManager.loadPvvaById(UserSettingsManager.getUserSettings().adapter()));
        this(host);
    }

    public PornClientImpl(@NonNull PVVAHost host) {
        requestProvider = createProvider(host.optHttpConfig().orElse(DEFAULT_HTTP_CONFIG));
        this.host = host;
        this.resourceConfig = host.resourceConfig();
        this.checker = new PVVAResourceChecker(resourceConfig);
        this.baseUrl = new AtomicReference<>(resourceConfig.baseUrl());
        this.scriptEngine = new ScriptEngineExecutor(host.mainParser());
    }

    private @NonNull PornRequestProvider createProvider(@NonNull HttpConfig config) {
        HttpClientType type = config.httpClient();
        return switch (type) {
            case JAVA_HTTP_CLIENT -> new HttpClientRequestProvider(config);
            case APACHE_HTTP_CLIENT ->
                    throw new UnsupportedOperationException("Apache http request provider do not suppoerted yet"); // TODO: Add Apache http request provider
            case OK_HTTP_CLIENT -> new OkHttpRequestProvider(config);
            case NETTY ->
                    throw new UnsupportedOperationException("Netty request provider do not suppoerted yet"); // TODO: Add Netty http request provider
            default -> throw new NoSuchRequestProviderException("No request provider: " + type.name(), type);
        };
    }

    @Override
    public List<ShortVideoInfo> requestMainPage(int page) {
        checkPage(page);

        UriBuilder builder = new UriBuilder(baseUrl.get());
        MainResources resources = resourceConfig.mainResources();
        if (checker.supportMain()) {
            String endpoint = VariableHandler.processVariables(resources.endpoint().orElseThrow(), Map.of(Variable.PAGE, String.valueOf(page)));
            builder.appendUriPart(endpoint);
            String response = requestProvider.executeGet(get(builder.build()));
            return scriptEngine.parseVideos(response);
        } else {
            throw new UnsupportedOperationException("This adapter not support main page");
        }
    }

    @Override
    public List<ShortVideoInfo> searchMainPage(String rawSearch, int page) {
        checkPage(page);

        UriBuilder builder = new UriBuilder(baseUrl.get());
        MainResources resources = resourceConfig.mainResources();
        if (checker.supportMainSearch()) {
            String preparedSearch = URLEncoder.encode(rawSearch, DEFAULT_CHARSET);
            String searchUrl = VariableHandler.processVariables(resources.searchUrl().orElseThrow(), Map.of(Variable.USER_INPUT, preparedSearch, Variable.PAGE, String.valueOf(page)));
            builder.appendUriPart(searchUrl);
            return scriptEngine.parseVideos(requestProvider.executeGet(get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support search main page");
        }
    }

    @Override
    public List<ModelInfo> requestModelsPage(int page) {
        checkPage(page);

        UriBuilder builder = new UriBuilder(baseUrl.get());
        ModelsResources resources = resourceConfig.modelsResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support models page"));
        if (checker.supportModels()) {
            String endpoint = VariableHandler.processVariables(resources.endpoint().orElseThrow(), Map.of(Variable.PAGE, String.valueOf(page)));
            builder.appendUriPart(endpoint);
            return scriptEngine.parseModels(requestProvider.executeGet(get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support models page");
        }
    }

    @Override
    public List<ShortVideoInfo> requestModelPage(String modelName) {
        UriBuilder builder = new UriBuilder(baseUrl.get());
        ModelsResources resources = resourceConfig.modelsResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support model page"));
        if (checker.supportModel()) {
            String preparedModel = URLEncoder.encode(modelName, DEFAULT_CHARSET);
            String modelUrl = VariableHandler.processVariables(resources.modelEndpoint().orElseThrow(), Map.of(Variable.MODEL_NAME, preparedModel));
            builder.appendUriPart(modelUrl);
            return scriptEngine.parseVideos(requestProvider.executeGet(get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support model page");
        }
    }

    @Override
    public List<ModelInfo> searchModelsPage(String rawSearch) {
        UriBuilder builder = new UriBuilder(baseUrl.get());
        ModelsResources resources = resourceConfig.modelsResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support search models page"));
        if (checker.supportModelsSearch()) {
            String preparedSearch = URLEncoder.encode(rawSearch, DEFAULT_CHARSET);
            String searchUrl = VariableHandler.processVariables(resources.modelSearchEndpoint().orElseThrow(), Map.of(Variable.USER_INPUT, preparedSearch));
            builder.appendUriPart(searchUrl);
            return scriptEngine.parseModels(requestProvider.executeGet(get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support search models page");
        }
    }

    @Override
    public List<CategoryInfo> requestCategories() {
        UriBuilder builder = new UriBuilder(baseUrl.get());
        CategoriesResources resources = resourceConfig.categoriesResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support categories page"));
        if (checker.supportCategories()) {
            String endpoint = resources.endpoint().orElseThrow();
            builder.appendUriPart(endpoint);
            return scriptEngine.parseCategories(requestProvider.executeGet(get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support categories page");
        }
    }

    @Override
    public List<ShortVideoInfo> requestCategoryPage(String categoryName, int page) {
        checkPage(page);

        UriBuilder builder = new UriBuilder(baseUrl.get());
        CategoriesResources resources = resourceConfig.categoriesResources().orElseThrow(() -> new UnsupportedOperationException("This adapter not support category page"));
        if (resources.supports() && resources.endpoint().isPresent()) {
            if (checker.supportCategory()) {
                String categoryUrl = VariableHandler.processVariables(resources.categoryEndpoint().orElseThrow(), Map.of(Variable.CATEGORY, categoryName, Variable.PAGE, String.valueOf(page)));
                builder.appendUriPart(categoryUrl);
                return scriptEngine.parseVideos(requestProvider.executeGet(get(builder.build())));
            } else {
                throw new UnsupportedOperationException("This adapter not support category page");
            }
        } else {
            throw new UnsupportedOperationException("This adapter not support category page");
        }
    }

    @Override
    public FullVideoInfo requestVideoPage(String videoId) {
        UriBuilder builder = new UriBuilder(baseUrl.get());
        if (checker.supportVideo()) {
            String videoUrl = VariableHandler.processVariables(resourceConfig.videoEndpoint(), Map.of(Variable.VIDEO_ID, videoId));
            builder.appendUriPart(videoUrl);
            return scriptEngine.parseFullVideoInfo(requestProvider.executeGet(get(builder.build())));
        } else {
            throw new UnsupportedOperationException("This adapter not support video page");
        }
    }

    @Override
    public @Nullable CompletableFuture<DownloadedVideoInfo> startDownload(URI videoUri, @NonNull FullVideoInfo info) {
        log.info("Start loading to file.");
        long videoSize = requestProvider.checkContentLength(head(videoUri));
        GlobalEventManager.broadcastEvent(new VideoDownloadingChannel(info.videoId(), videoSize, DownloadingType.START));

        PornDownloader downloader = new PornDownloader(requestProvider, get(videoUri));
        return downloader.startDownload(videoSize, info, requestProvider.executeRaw(get(info.previewUrl())));
    }

    //======================================\\

    @Override
    public synchronized void setProxy(Proxy proxy) {
        requestProvider.setProxy(proxy);
    }

    @Override
    public void setMirror(int index) {
        if (checker.supportMirrors()) {
            List<String> mirrors = resourceConfig.mirrors().orElseThrow();
            if (!mirrors.isEmpty()) {
                this.baseUrl.set(mirrors.get(index)); // Thorws index exception if index is outside
            }
        } else {
            throw new UnsupportedOperationException("This adapter not support mirrors");
        }
    }

    private void checkPage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
    }

    public PornRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public PVVAHost getHost() {
        return host;
    }

    @Override
    public void close() {
        requestProvider.close();
    }
}