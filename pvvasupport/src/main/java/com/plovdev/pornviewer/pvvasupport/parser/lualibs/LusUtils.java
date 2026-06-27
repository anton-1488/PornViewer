package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

public final class LusUtils {
    /**
     * Конвертирует Lua таблицу в Java Object.
     */
    public static @NonNull Object luaTableToObject(@NonNull LuaTable table) {
        boolean isArray = true;
        int maxKey = 0;

        LuaValue[] keys = table.keys();
        for (LuaValue key : keys) {
            if (key.isnumber()) {
                int idx = key.toint();
                if (idx > maxKey) maxKey = idx;
            } else {
                isArray = false;
                break;
            }
        }

        if (isArray && maxKey > 0) {
            Object[] array = new Object[maxKey];
            for (int i = 1; i <= maxKey; i++) {
                LuaValue val = table.get(i);
                array[i - 1] = luaValueToObject(val);
            }
            return array;
        } else {
            Map<String, Object> map = new HashMap<>();
            for (LuaValue key : keys) {
                String keyStr = key.tojstring();
                LuaValue val = table.get(key);
                map.put(keyStr, luaValueToObject(val));
            }
            return map;
        }
    }

    /**
     * Конвертирует LuaValue в Java объект
     */
    public static @Nullable Object luaValueToObject(@NonNull LuaValue value) {
        if (value.isnil()) {
            return null;
        }

        if (value.isboolean()) {
            return value.toboolean();
        }

        if (value.isnumber()) {
            return value.todouble();
        }

        if (value.isstring()) {
            return value.tojstring();
        }

        if (value.istable()) {
            return luaTableToObject(value.checktable());
        }

        return value.tojstring();
    }
}