package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.HashMap;
import java.util.Map;

public class JsonLib extends TwoArgFunction {
    private static final Gson gson = new Gson();

    @Override
    public LuaValue call(@NonNull LuaValue modname, @NonNull LuaValue env) {
        LuaValue library = new LuaTable();
        library.set("fromJson", new FromJsonFunction());
        library.set("toJson", new ToJsonFunction());
        library.set("newJson", new NewJsonFunction());
        library.set("mapJson", new MapJsonFunction());
        library.set("parse", new ParseJsonFunction());
        library.set("stringify", new StringifyJsonFunction());

        env.set("json", library);
        return library;
    }

    /**
     * Парсит JSON строку в Lua таблицу
     */
    private static class FromJsonFunction extends OneArgFunction {
        @Override
        public LuaValue call(@NonNull LuaValue arg) {
            String json = arg.tojstring();
            if (json == null || json.isEmpty()) {
                return new LuaTable();
            }

            try {
                JsonElement element = JsonParser.parseString(json);
                return jsonElementToLuaValue(element);
            } catch (Exception e) {
                return new LuaTable();
            }
        }
    }

    /**
     * Преобразует Lua значение в JSON строку
     */
    private static class ToJsonFunction extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            Object obj = luaValueToObject(arg);
            String json = gson.toJson(obj);
            return LuaValue.valueOf(json);
        }
    }

    /**
     * Создает новый пустой JSON объект
     */
    private static class NewJsonFunction extends ZeroArgFunction {
        @Contract(value = " -> new", pure = true)
        @Override
        public @NonNull LuaValue call() {
            return new LuaTable();
        }
    }

    /**
     * Преобразует Lua таблицу в JSON строку
     */
    private static class MapJsonFunction extends OneArgFunction {
        @Override
        public LuaValue call(@NonNull LuaValue arg) {
            if (!arg.istable()) {
                return LuaValue.valueOf("{}");
            }
            Object obj = luaTableToObject(arg.checktable());
            String json = gson.toJson(obj);
            return LuaValue.valueOf(json);
        }
    }

    /**
     * Парсит JSON строку
     */
    private static class ParseJsonFunction extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            return new FromJsonFunction().call(arg);
        }
    }

    /**
     * Преобразует Lua значение в JSON строку
     */
    private static class StringifyJsonFunction extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            return new ToJsonFunction().call(arg);
        }
    }

    /**
     * Конвертирует JsonElement в LuaValue
     */
    private static LuaValue jsonElementToLuaValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return LuaValue.NIL;
        }

        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return LuaValue.valueOf(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                return LuaValue.valueOf(primitive.getAsDouble());
            } else {
                return LuaValue.valueOf(primitive.getAsString());
            }
        }

        if (element.isJsonArray()) {
            LuaTable table = new LuaTable();
            int i = 1;
            for (JsonElement item : element.getAsJsonArray()) {
                table.set(i++, jsonElementToLuaValue(item));
            }
            return table;
        }

        if (element.isJsonObject()) {
            LuaTable table = new LuaTable();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                table.set(entry.getKey(), jsonElementToLuaValue(entry.getValue()));
            }
            return table;
        }

        return LuaValue.NIL;
    }

    /**
     * Конвертирует LuaValue в Java объект для Gson
     */
    private static @Nullable Object luaValueToObject(@NonNull LuaValue value) {
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

    /**
     * Конвертирует Lua таблицу в Java Map/List для Gson
     */
    private static @NonNull Object luaTableToObject(@NonNull LuaTable table) {
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
}