package com.plovdev.pornviewer.models;

import java.util.List;

public record DownloadedCardInfo(String title,
                                 String path,
                                 String size,
                                 String date,
                                 String duration,
                                 String description,
                                 List<DownloadedVideoInfo.Timecode> timecodes,
                                 byte[] preview) {
}