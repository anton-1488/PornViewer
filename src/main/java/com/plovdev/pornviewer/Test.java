package com.plovdev.pornviewer;

import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    static void main(String[] args) {
        PBPornHandler handler = new PBPornHandler();
        System.out.println(handler.requestPorn("https://hot.noodlemagazine.com/"));
    }
}