package com.plovdev.pornviewer.http;

import com.plovdev.pornviewer.core.models.porn.CategoryInfo;
import com.plovdev.pornviewer.core.models.porn.FullVideoInfo;
import com.plovdev.pornviewer.core.models.porn.ModelInfo;
import com.plovdev.pornviewer.core.models.porn.ShortVideoInfo;
import com.plovdev.pornviewer.services.json.DownloadedVideoInfo;

import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Класс для взаимодействия адаптера с сайтом
 */
public interface PornClient extends AutoCloseable {
    /**
     * Запрашивает главную страницу с сайта.
     *
     * @param page номер запрашиваемой страницы.
     * @return спиоск видео, которые есть на странице.
     * @throws UnsupportedOperationException если плагин не поддерживает взаимодействие с главной страницей.
     */
    List<ShortVideoInfo> requestMainPage(int page);

    /**
     * Производит поиск по главной странице/каталогу сайта.
     *
     * @param rawSearch сырой пользовательский текст пользователя. Должен быть закодирован в реализации.
     * @param page      номер страницы для поиска.
     * @return список найденых видео.
     * @throws UnsupportedOperationException если плагин не поддерживает поиск по главной странице/каталогу сайта.
     */
    List<ShortVideoInfo> searchMainPage(String rawSearch, int page);

    /**
     * Запрашивает страницу с моделями, доступными на сайте.
     *
     * @param page номер запрашиваемой страницы с моделями.
     * @return список моделей, которые есть на странице.
     * @throws UnsupportedOperationException если плагин не поддерживает моделей.
     */
    List<ModelInfo> requestModelsPage(int page);

    /**
     * Получает список видео у конкретной модели.
     *
     * @param modelName имя модели у которой будет получаться список видео. Должно быть закодирован в реализции.
     * @return список видео у конкретной модели.
     * @throws UnsupportedOperationException если на сайте нет видео у моделей или плагин это не поддерживает.
     */
    List<ShortVideoInfo> requestModelPage(String modelName);

    /**
     * Производит поиск по моделям, доступным на сайте.
     *
     * @param rawSearch имя модели по которому будет поиск на сайте. Должно быть закодирован в реализции.
     * @return список найденых моделей.
     * @throws UnsupportedOperationException если плагин не поддерживает поиск по моделям.
     */
    List<ModelInfo> searchModelsPage(String rawSearch);

    /**
     * Запрашивает список доступных категорий с сайта.
     *
     * @return список доступных категорий.
     * @throws UnsupportedOperationException если плагин или сайт не поддерживает категории.
     */
    List<CategoryInfo> requestCategories();

    /**
     * Запрашивает список видео по конкретной категории.
     *
     * @param categoryName название категории. Должно быть закодировано в реализации.
     * @param page         номер запрашиваемой страницы.
     * @return список видео, входщих в эту категорию.
     * @throws UnsupportedOperationException если плагин не поддерживает список видео по конкретной категории.
     */
    List<ShortVideoInfo> requestCategoryPage(String categoryName, int page);

    /**
     * Запрашивает страницу видео с подробной ифнормацией и ссылками.
     *
     * @param videoId уникальное id запрашиваемого видео.
     * @return полную информацию о странице видео.
     * @throws UnsupportedOperationException если плагин не поддерживает страницу с ифнормацией о видео.
     */
    FullVideoInfo requestVideoPage(String videoId);

    /**
     * Начинает загрузку видео в зашифрованный контейнер .pvvf.
     * Должен получить превью, трейлер(если пользователь запросил их в настройках), и сохранить в контейнер, используя средства модуля pvvfsupport.
     *
     * @param videoUri прямая ссылка на видео полученная через плагин.
     * @param info     ифнормация о видео которая сохранится в метаданные конейнера.
     * @return будущий результат загруженного видео.
     */
    CompletableFuture<DownloadedVideoInfo> startDownload(URI videoUri, FullVideoInfo info);

    /**
     * Меняет прокси у RequestProvider.
     *
     * @param proxy новый прокси.
     */
    void setProxy(Proxy proxy);

    /**
     * Меняет зеркало для запросов к сайту.
     *
     * @param index индекс нового зеркала в списке.
     * @throws UnsupportedOperationException если плагин не поддерживает зеркала.
     */
    void setMirror(int index);

    /**
     * Проивзодит закрытие клиента и RequestProvider.
     */
    @Override
    void close();
}