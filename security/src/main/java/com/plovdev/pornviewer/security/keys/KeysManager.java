package com.plovdev.pornviewer.security.keys;

import java.util.Optional;

/**
 * Менеджер управления ключами.
 * Предоставляет доступ к необходимым приложению ключам.
 */
public interface KeysManager {
    /**
     * Инициализирует ключи для каждого модуля, если они не созданы.
     */
    void initKeysIfNotExist();

    /**
     * Получает хеш пинкода, которым пользователь защитил приложение.
     *
     * @return хеш пинкода.
     */
    Optional<byte[]> getUserPinHash();

    /**
     * Обновляет пин-код приложения.
     *
     * @param pin введеный в ui пинкод, или null если его необходимо удалить.
     */
    void setAppPin(String pin);

    /**
     * Получает ключ для конкретного модуля.
     *
     * @param moduleId идентификатор модуля.
     * @return ключ конкретного модуля.
     */
    byte[] getKeyForModule(String moduleId);
}