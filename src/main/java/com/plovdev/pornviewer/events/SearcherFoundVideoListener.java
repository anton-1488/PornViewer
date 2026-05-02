package com.plovdev.pornviewer.events;

import com.plovdev.pornviewer.models.VideoCard;

public interface SearcherFoundVideoListener {
    void onFound(VideoCard card);
}