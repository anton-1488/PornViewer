package com.plovdev.pornviewer.core.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Globals {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final List<Runnable> SHUTDOWN_HOOKS = new CopyOnWriteArrayList<>();

    public static final UUID APP_ACCESS_TOKEN = UUID.randomUUID();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(VIRTUAL_EXECUTOR::close));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SHUTDOWN_HOOKS.forEach(Runnable::run)));
    }

    private Globals() {
        throw new UnsupportedOperationException();
    }

    public static void addShutdownHook(Runnable hook) {
        SHUTDOWN_HOOKS.add(hook);
    }

    public static void removeShutdownHook(Runnable hook) {
        SHUTDOWN_HOOKS.remove(hook);
    }
}