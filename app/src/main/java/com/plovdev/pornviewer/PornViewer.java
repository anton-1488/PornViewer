package com.plovdev.pornviewer;

import com.plovdev.pornviewer.pvvasupport.parser.ScriptEngineExecutor;
import org.plovdev.pvva.models.parsers.MainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger("CLEAR");

    static void main() throws Exception {
        String lua = """
                function main(data)
                    return hashlib.sha256(data)
                end
                """;

        ScriptEngineExecutor engineExecutor = new ScriptEngineExecutor(new MainParser(lua));
        System.out.println(engineExecutor.execute("main", "Hello, world!"));
    }
}