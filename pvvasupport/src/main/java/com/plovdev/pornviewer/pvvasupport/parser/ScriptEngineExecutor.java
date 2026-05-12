package com.plovdev.pornviewer.pvvasupport.parser;

import com.google.gson.reflect.TypeToken;
import com.plovdev.pornviewer.commons.models.CategoryInfo;
import com.plovdev.pornviewer.commons.models.FullVideoInfo;
import com.plovdev.pornviewer.commons.models.ModelInfo;
import com.plovdev.pornviewer.commons.models.ShortVideoInfo;
import com.plovdev.pornviewer.exceptions.ScriptExecutionException;
import com.plovdev.pornviewer.utils.json.JSONSerializer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.plovdev.pvva.models.parsers.MainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ScriptEngineExecutor {
    private static final Globals GLOBALS = new Globals();
    private static final Logger log = LoggerFactory.getLogger(ScriptEngineExecutor.class);
    private final LuaValue scriptFunctions;

    static {
        GLOBALS.load(new JseBaseLib());
        GLOBALS.load(new PackageLib());
        GLOBALS.load(new StringLib());
        GLOBALS.load(new TableLib());

        LoadState.install(GLOBALS);
        LuaC.install(GLOBALS);
    }

    @Contract(pure = true)
    public ScriptEngineExecutor(@NonNull MainParser parser) {
        LuaValue sandboxEnv = LuaValue.tableOf();
        sandboxEnv.setmetatable(GLOBALS);

        String scriptCode = parser.rawScript();
        LuaValue chunk = GLOBALS.load(scriptCode, "script");
        if (chunk.isnil() || !chunk.isfunction()) {
            throw new RuntimeException("Failed to load Lua script");
        }

        chunk.call();
        this.scriptFunctions = GLOBALS;
    }

    private <V> V executeAndDeserialize(MethodName methodName, String input, TypeToken<V> type) {
        try {
            String parsedJson = execute(methodName.getMethodName(), input);
            if (parsedJson != null) {
                return JSONSerializer.deserialize(parsedJson, type.getType());
            } else {
                throw new IllegalArgumentException("Parsed JSON can't be null");
            }
        } catch (Exception e) {
            throw new ScriptExecutionException(e);
        }
    }

    /**
     * Invoke lua method from loaded script in safe sandbox.
     *
     * @param method    method name (function name in Lua script)
     * @param inputData input page (HTML, JSON, XML, raw string) to parsing
     * @return serialized JSON of required object dto.
     */
    private @Nullable String execute(String method, String inputData) throws NoSuchMethodException {
        LuaValue func = this.scriptFunctions.get(method);
        if (func.isnil()) {
            throw new NoSuchMethodException("Function '" + method + "' not found in Lua script");
        }

        LuaValue result = func.call(LuaValue.valueOf(inputData));
        if (result.isstring()) {
            return result.tojstring();
        } else if (result.isnil()) {
            return null;
        } else {
            return result.toString();
        }
    }


    //===========PUBLIC API===========\\

    public List<CategoryInfo> parseCategories(String rawInput) {
        return executeAndDeserialize(MethodName.CATEGORIES, rawInput, new TypeToken<>() {
        });
    }

    public FullVideoInfo parseFullVideoInfo(String rawInput) {
        return executeAndDeserialize(MethodName.FULL_VIDEO_INFO, rawInput, new TypeToken<>() {
        });
    }

    public List<ModelInfo> parseModels(String rawInput) {
        return executeAndDeserialize(MethodName.MODELS, rawInput, new TypeToken<>() {
        });
    }

    public List<ShortVideoInfo> parseVideos(String rawInput) {
        return executeAndDeserialize(MethodName.VIDEOS, rawInput, new TypeToken<>() {
        });
    }
}