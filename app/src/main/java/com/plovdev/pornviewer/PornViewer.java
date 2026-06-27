package com.plovdev.pornviewer;

import com.plovdev.pornviewer.pvvasupport.parser.DurationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger("CLEAR");

    static void main() {
        System.out.println(DurationParser.parseDuration("5:1:23:45"));
    }
}