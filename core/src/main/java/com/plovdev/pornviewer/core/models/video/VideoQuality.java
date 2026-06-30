package com.plovdev.pornviewer.core.models.video;

import java.util.NoSuchElementException;

public enum VideoQuality {
    VIDEO_4K("4k", "2160p", "uhd", "ultra hd", "ultra-high-definition"),
    VIDEO_2K("2k", "1440p", "qhd", "quad hd", "wqhd"),
    FullHD("1080p", "full hd", "fhd", "1080", "1920x1080"),
    HD("720p", "hd ready", "hd720", "720", "1280x720", "hd video"),
    HQ("hq", "high quality", "good quality", "480p", "854x480", "high quality"),
    SD("sd", "standard quality", "480", "360p", "640x360", "standard definition", "480p"),
    LQ("lq", "low quality", "240p", "320x240", "mobile", "low", "144p", "160p");

    public static VideoQuality fromString(String qual) {
        if (qual == null || qual.isBlank()) {
            throw new IllegalArgumentException("Quality text can't be null or empty");
        }

        qual = qual.trim();
        for (VideoQuality quality : values()) {
            if (quality.name().equalsIgnoreCase(qual)) {
                return quality;
            } else {
                for (String alias : quality.getAliases()) {
                    if (alias.equalsIgnoreCase(qual)) {
                        return quality;
                    }
                }
            }
        }

        throw new NoSuchElementException("Quality " + qual + " not found");
    }

    private final String[] aliases;

    VideoQuality(String... aliases) {
        this.aliases = aliases;
    }

    public String[] getAliases() {
        return aliases;
    }
}