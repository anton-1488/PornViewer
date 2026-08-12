package com.plovdev.pornviewer;

import com.plovdev.pornviewer.databases.FavoriteVideos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    static void main(String[] args) {
        System.out.println(FavoriteVideos.getAll());
    }
}