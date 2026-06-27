package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import com.plovdev.pornviewer.pvvasupport.parser.DurationParser;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class PornViewerLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue luaValue, @NonNull LuaValue env) {
        LuaValue library = new LuaTable();
        library.set("parseDuration", new parseDurationToISO());

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
}