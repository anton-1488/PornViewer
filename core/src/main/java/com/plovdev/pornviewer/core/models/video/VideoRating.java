package com.plovdev.pornviewer.core.models.video;

public record VideoRating(int rating, VideoRating.RatingType ratingType) {
    public enum RatingType {
        TOTAL_LIKES, PERCENTAGE
    }
}