package org.JavaPE.controller;

import org.JavaPE.controller.Request.ReviewRequest;
import org.JavaPE.domain.Review;
import org.JavaPE.services.ReviewService;
import org.JavaPE.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 1. Submit Post for Review
    @PostMapping("/submit")
    public ResponseEntity<String> submitPostForReview(@RequestBody ReviewRequest reviewRequest) {
        reviewService.submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());
        return ResponseEntity.ok("Post submitted for review");
    }

    // 2. Get All Reviews
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // 1. Approve a Review
    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<String> approveReview(@PathVariable Long reviewId, @RequestParam String reviewer) {
        reviewService.approveReview(reviewId, reviewer);
        return ResponseEntity.ok("Review approved");
    }

    // 2. Reject a Review
    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<String> rejectReview(@PathVariable Long reviewId, @RequestParam String reviewer) {
        reviewService.rejectReview(reviewId, reviewer);
        return ResponseEntity.ok("Review rejected");
    }
}
