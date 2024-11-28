package org.JavaPE.services;

import org.JavaPE.domain.Review;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewService {
    void submitForReview(Long postId, String author);

    void approveReview(Long reviewId, String reviewer);

    void rejectReview(Long reviewId, String reviewer);
    List<Review> getAllReviews();
}
