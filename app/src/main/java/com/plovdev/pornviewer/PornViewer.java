package com.plovdev.pornviewer;

import com.plovdev.pornviewer.commons.models.app.AppInfo;
import com.plovdev.pornviewer.utils.files.EnvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger("CLEAR");

    static void main(String[] args) {
        EnvReader reader = new EnvReader();
        AppInfo info = reader.loadAppInfo();
        System.out.println(info);
    }
}