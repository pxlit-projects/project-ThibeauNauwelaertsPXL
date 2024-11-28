package org.JavaPE.controller;

import org.JavaPE.controller.Request.ReviewRequest;
import org.JavaPE.controller.dto.RejectRequest;
import org.JavaPE.domain.Review;
import org.JavaPE.services.ReviewService;
import org.JavaPE.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitPostForReview(@RequestBody ReviewRequest reviewRequest) {
        reviewService.submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());
        return ResponseEntity.ok("Post submitted for review");
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<String> approveReview(@PathVariable Long reviewId, @RequestParam String reviewer) {
        reviewService.approveReview(reviewId, reviewer);
        return ResponseEntity.ok("Review approved");
    }

    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<String> rejectReview(
            @PathVariable Long reviewId,
            @RequestBody RejectRequest rejectRequest
    ) {
        reviewService.rejectReview(reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());
        return ResponseEntity.ok("Review rejected with remarks: " + rejectRequest.getRemarks());
    }

    // SSE Endpoint for clients to connect to and receive notifications
    @GetMapping("/notifications")
    public SseEmitter getNotifications() {
        return reviewService.registerClient();  // Register new SSE client
    }
}
