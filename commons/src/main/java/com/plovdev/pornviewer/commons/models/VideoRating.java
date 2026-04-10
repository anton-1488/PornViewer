package com.plovdev.pornviewer.commons.models;

import org.jetbrains.annotations.NotNull;

public record VideoRating(int rating, VideoRating.RatingType ratingType) {
    public enum RatingType {
        TOTAL_LIKES, PERCENTAGE
    }

    @Override
    @NotNull
    public String toString() {
        return "VideoRating{" +
                "rating=" + rating +
                ", ratingType=" + ratingType +
                '}';
    }
}