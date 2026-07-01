package com.plovdev.pornviewer.pvvasupport.verifiers;

/**
 * Верификатор плагинов при загрузке или уже загруженных адаптеров.
 */
public interface PluginVerifier {
    /**
     * Верифицирует плагин если пользователь не отключил проверку в настройках.
     *
     * @param pluginData входные данные самого плагина.
     * @return валиден ли плагин или нет. Всегда возвращает true если проверка отключена.
     */
    default boolean checkPluginIfNeed(byte[] pluginData) {
        // TODO: request from settings if need check plugins.
        boolean needCheck = true;
        //noinspection ConstantValue
        if (needCheck) {
            return verifyPlugin(pluginData);
        } else {
            return true;
        }
    }

    /**
     * Верифицирует плагин и подтверждает что он не был изменен или действительно является подлиным.
     *
     * @param pluginData входные данные самого плагина.
     * @return валиден ли плагин или нет.
     */
    boolean verifyPlugin(byte[] pluginData);
}