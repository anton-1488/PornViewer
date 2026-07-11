package com.plovdev.pornviewer.security;

public enum RegisteredSecurityModule {
    DATABASE("database"), PVVF_SUPPORT("pvvfsupport");

    private final String moduleId;

    RegisteredSecurityModule(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleId() {
        return moduleId;
    }
}