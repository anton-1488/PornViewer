package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import org.jspecify.annotations.NonNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class HashLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, @NonNull LuaValue env) {
        LuaValue library = new LuaTable();

        env.set("hashlin", library);
        return library;
    }
}