package org.JavaPE.services;

import org.JavaPE.domain.Review;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewService {
    void submitForReview(Long postId, String author);

    void approveReview(Long reviewId, String reviewer);

    void rejectReview(Long reviewId, String reviewer, String remarks);
    List<Review> getAllReviews();

    SseEmitter registerClient();
}
