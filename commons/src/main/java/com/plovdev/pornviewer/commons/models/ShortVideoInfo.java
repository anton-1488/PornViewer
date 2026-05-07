package com.plovdev.pornviewer.commons.models;

import java.net.URI;
import java.time.Duration;

public record ShortVideoInfo(String id, String title, URI url, URI picture, Duration duration, int views, String rating) {

}