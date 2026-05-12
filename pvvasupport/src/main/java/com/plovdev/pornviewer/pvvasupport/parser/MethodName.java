package com.plovdev.pornviewer.pvvasupport.parser;

public enum MethodName {
    CATEGORIES("parseCategories"),
    FULL_VIDEO_INFO("parseVideoPage"),
    MODELS("parseModels"),
    VIDEOS("parseVideos");

    private final String methodName;

    MethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}