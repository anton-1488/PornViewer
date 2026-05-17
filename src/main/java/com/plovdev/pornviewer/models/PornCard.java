package com.plovdev.pornviewer.models;

import javafx.scene.layout.Pane;

import java.util.Objects;

public abstract class PornCard extends Pane {
    protected int id;
    protected String title;
    protected String url;
    protected String pic;

    public PornCard(int id, String title, String url, String pic) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.pic = pic;
    }
    public PornCard() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getCardId() {
        return id;
    }

    public void setCardId(int id) {
        this.id = id;
    }

    public abstract void render();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PornCard pornCard = (PornCard) o;
        return id == pornCard.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PornCard{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}