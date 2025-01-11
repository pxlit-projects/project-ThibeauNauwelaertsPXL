package org.JavaPE.services;

import org.JavaPE.controller.dto.NotificationMessage;
import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.JavaPE.domain.Review;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewService {
    void submitForReview(Long postId, String author);

    void approveReview(Long reviewId, String reviewer);

    void rejectReview(Long reviewId, String reviewer, String remarks);
    void removePendingReviewForPost(Long postId);
    List<ReviewWithPostDetailsDTO> getAllReviewsWithPostDetails();

     boolean hasActiveReviewForPost(Long postId);
     void publishToSseClients(NotificationMessage message);
     SseEmitter registerClient();
}
