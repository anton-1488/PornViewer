package com.plovdev.pornviewer.databases;

import com.plovdev.pornviewer.httpquering.PornVideoAdapter;
import com.plovdev.pornviewer.pornimpl.porn365.Porn365VideoAdapter;

public class User {
    private final PornVideoAdapter p365Adapter = new Porn365VideoAdapter();

    private String pvva;
    private String id;

    public User(String pvva, String id) {
        this.pvva = pvva;
        this.id = id;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PornVideoAdapter getPornAdapter() {
        return p365Adapter;
    }

    public String getPvva() {
        return pvva;
    }

    public void setPvva(String pvva) {
        this.pvva = pvva;
    }

    @Override
    public String toString() {
        return String.format("User id: %s, Adapter: %s", id, pvva);
    }
}