package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import com.plovdev.pornviewer.security.DigestUtils;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class HashLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, @NonNull LuaValue env) {
        LuaValue library = new LuaTable();
        library.set("md2", processAlgorithm(DigestUtils.MD2));
        library.set("md5", processAlgorithm(DigestUtils.MD5));
        library.set("sha1", processAlgorithm(DigestUtils.SHA1));
        library.set("sha224", processAlgorithm(DigestUtils.SHA_224));
        library.set("sha256", processAlgorithm(DigestUtils.SHA_256));
        library.set("sha384", processAlgorithm(DigestUtils.SHA_384));
        library.set("sha512", processAlgorithm(DigestUtils.SHA_512));
        library.set("sha3_224", processAlgorithm(DigestUtils.SHA3_224));
        library.set("sha3_256", processAlgorithm(DigestUtils.SHA3_256));
        library.set("sha3_384", processAlgorithm(DigestUtils.SHA3_384));
        library.set("sha3_512", processAlgorithm(DigestUtils.SHA3_512));
        library.set("sha512_224", processAlgorithm(DigestUtils.SHA512_224));
        library.set("sha512_256", processAlgorithm(DigestUtils.SHA512_256));
        library.set("shake128_256", processAlgorithm(DigestUtils.SHAKE128_256));
        library.set("shake256_512", processAlgorithm(DigestUtils.SHAKE256_512));

        env.set("hashlib", library);
        return library;
    }

    @Contract(value = "_ -> new", pure = true)
    private @NonNull OneArgFunction processAlgorithm(String algoritm) {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                String plain = luaValue.tojstring();
                if (plain == null || plain.isEmpty()) {
                    return LuaValue.NIL;
                }

                return LuaValue.valueOf(DigestUtils.processAlgorithm(algoritm, plain));
            }
        };
    }
}