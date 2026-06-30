package com.plovdev.pornviewer.pvvasupport.loading;

import com.plovdev.pornviewer.pvvasupport.exceptions.AdapterLoadingException;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginNotVerifiedException;
import org.plovdev.pvva.models.PVVAHost;

import java.net.URI;
import java.nio.file.Path;

/**
 * Интерфейс для загрузки плагинов из сети и диска.
 * Предоставляет базовые методы для прямой загрузки с источника.
 * Так же реализация должно включать верификацию плагина.
 * <p>
 * Кеширование и прочие возможности должны быть вынесены в другие классы.
 */
public interface PluginLoader {
    /**
     * Загружает плагин с сервера. Верифицирует плагин по подписи.
     *
     * @param pluginUri путь для загрузки плагина.
     * @return загруженный из интернета плагин.
     * @throws PluginNotVerifiedException если плагин не был успешно верифицирован.
     * @throws AdapterLoadingException    если произошла какая то ошибка при загрузке.
     */
    PVVAHost loadFromServer(URI pluginUri);

    /**
     * Загружает плагин с локального диска. Верифицирует плагин по его хешу.
     *
     * @param path путь для загрузки плагина.
     * @return загруженный с диска плагин.
     * @throws PluginNotVerifiedException если плагин не был успешно верифицирован.
     * @throws AdapterLoadingException    если произошла какая то ошибка при загрузке.
     */
    PVVAHost loadFromDisk(Path path);
}