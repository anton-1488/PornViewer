package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import com.plovdev.pornviewer.pvvasupport.parser.DurationParser;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.time.Duration;
import java.util.UUID;

public class PornViewerLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue luaValue, @NonNull LuaValue env) {
        LuaValue library = new LuaTable();
        library.set("parseDuration", new parseDurationToISO());
        library.set("currentTimeMillis", new currentTimeMillis());
        library.set("uuid", new uuid());
        library.set("millisToDuration", new millisToDuration());

        env.set("pvlib", library);
        return library;
    }

    private static class parseDurationToISO extends OneArgFunction {
        @Contract(pure = true)
        @Override
        public @Nullable LuaValue call(@NonNull LuaValue luaValue) {
            String duration = luaValue.tojstring();
            return LuaValue.valueOf(DurationParser.parseDuration(duration).toString());
        }
    }

    private static class currentTimeMillis extends ZeroArgFunction {
        @Contract(pure = true)
        @Override
        public @Nullable LuaValue call() {
            return LuaValue.valueOf(System.currentTimeMillis());
        }
    }

    private static class uuid extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(UUID.randomUUID().toString());
        }
    }

    private static class millisToDuration extends OneArgFunction {
        @Override
        public LuaValue call(@NonNull LuaValue luaValue) {
            long millis = luaValue.tolong();
            return LuaValue.valueOf(Duration.ofMillis(millis).toString());
        }
    }
}