package com.plovdev.pornviewer.security.keys;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;
import com.plovdev.pornviewer.security.CryptoUtils;
import com.plovdev.pornviewer.security.exceptions.PornViewerSecurityException;
import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.Keychain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KeysManagerImpl implements KeysManager {
    private static final Logger log = LoggerFactory.getLogger(KeysManagerImpl.class);
    private static final Keychain KEYCHAIN = Keychain.getKeychain(KeysManager.class);

    private static final String REGISTERED_MODULES_PATH = "/registered-modules.list";
    private static final String USER_PIN_HASH_PATH = "user.pin.hash";
    private static final String MODULE_KEY_PREFIX = "module.key.";

    private static final List<String> registeredModules;

    static {
        log.info("Collecting registered security modules...");
        try (InputStream stream = KeysManager.class.getResourceAsStream(REGISTERED_MODULES_PATH)) {
            String[] modules = new String(Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8).split(",");
            registeredModules = Arrays.stream(modules).map(moduleId -> moduleId.trim().toLowerCase()).toList();
            log.debug("Collected modules: {}", registeredModules);
        } catch (IOException e) {
            throw new PornViewerException("Can't collect registred security modules", e);
        }
    }

    /**
     * Инициализирует ключи для каждого модуля, если они не созданы.
     */
    @Override
    public synchronized void initKeysIfNotExist() {
        log.debug("Start initialize keys");
        for (String moduleId : registeredModules) {
            log.debug("Process key for module: {}", moduleId);
            String moduleIdKey = MODULE_KEY_PREFIX + moduleId;
            byte[] exist = KEYCHAIN.getRawPassword(moduleIdKey);
            if (exist == null) {
                byte[] moduleKey = new byte[32];
                try {
                    CryptoUtils.createRandomPassword(moduleKey);
                    KEYCHAIN.setPassword(moduleIdKey, moduleKey);
                } finally {
                    Arrays.fill(moduleKey, (byte) 0);
                }
            } else {
                Arrays.fill(exist, (byte) 0);
            }
        }
    }

    /**
     * Получает хеш пинкода, которым пользователь защитил приложение.
     *
     * @return хеш пинкода.
     */
    @Override
    public Optional<byte[]> getUserPinHash() {
        return Optional.ofNullable(KEYCHAIN.getRawPassword(USER_PIN_HASH_PATH));
    }

    /**
     * Обновляет пин-код приложения.
     *
     * @param pin введеный в ui пинкод, или null если его необходимо удалить.
     */
    @Override
    public void setAppPin(@Nullable String pin) {
        if (pin == null) {
            log.debug("Deleting user app pin");
            KEYCHAIN.deletePassword(USER_PIN_HASH_PATH);
        } else {
            if (pin.isBlank()) {
                throw new IllegalArgumentException("App pin is blank!");
            }
            char[] appPinChars = pin.toCharArray();
            byte[] pinHash = new KeysEncoderImpl().encode(appPinChars);
            try {
                KEYCHAIN.setPassword(USER_PIN_HASH_PATH, pinHash);
                log.debug("User app pin updates.");
            } finally {
                Arrays.fill(pinHash, (byte) 0);
                Arrays.fill(appPinChars, '\u0000');
            }
        }
    }

    /**
     * Получает ключ для конкретного модуля.
     *
     * @param moduleId идентификатор модуля.
     * @return ключ конкретного модуля.
     */
    @Override
    public byte[] getKeyForModule(String moduleId) {
        if (moduleId == null || moduleId.isBlank()) {
            throw new IllegalArgumentException("Module id is empty.");
        }
        moduleId = moduleId.trim().toLowerCase();

        if (!registeredModules.contains(moduleId)) {
            throw new PornViewerSecurityException("Access denied. List of registered modules don't contains specified module id");
        }
        String moduleIdKey = MODULE_KEY_PREFIX + moduleId;
        byte[] password = KEYCHAIN.getRawPassword(moduleIdKey);

        if (password == null) {
            throw new NoSuchElementException("Password for module " + moduleId + " not found in Keychain!");
        }
        return password;
    }
}