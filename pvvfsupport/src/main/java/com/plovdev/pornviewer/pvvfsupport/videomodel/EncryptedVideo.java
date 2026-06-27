package com.plovdev.pornviewer.pvvfsupport.videomodel;

public class EncryptedVideo {
    private VideoHeader videoHeader;
    private VideoMetadata videoMetadata;

    public EncryptedVideo(VideoHeader videoHeader, VideoMetadata videoMetadata) {
        this.videoHeader = videoHeader;
        this.videoMetadata = videoMetadata;
    }

    public EncryptedVideo() {
    }

    public VideoHeader getVideoHeader() {
        return videoHeader;
    }

    public void setVideoHeader(VideoHeader videoHeader) {
        this.videoHeader = videoHeader;
    }

    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    public void setVideoMetadata(VideoMetadata videoMetadata) {
        this.videoMetadata = videoMetadata;
    }

    @Override
    public String toString() {
        return videoHeader.toString() + videoMetadata.toString();
    }
}