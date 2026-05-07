package com.plovdev.pornviewer.commons.adapter;

public record AdapterInfo(String pluginId, int minVers, int maxVers, String name, String version, String descr,
                          String updateUrl, boolean signRequeired, String author, String authorPage, String licenseUrl,
                          String homepage, String pathName) {
}