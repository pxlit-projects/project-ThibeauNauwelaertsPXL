package org.JavaPE.controller;

import org.JavaPE.controller.Request.ReviewRequest;
import org.JavaPE.controller.dto.RejectRequest;
import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.JavaPE.services.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitPostForReview(
            @RequestBody ReviewRequest reviewRequest) {
        logger.info("Received request to submit post for review. Post ID: {}, Author: {}",
                reviewRequest.getPostId(), reviewRequest.getAuthor());
        try {
            reviewService.submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());
            logger.info("Post with ID: {} successfully submitted for review by Author: {}",
                    reviewRequest.getPostId(), reviewRequest.getAuthor());
            return ResponseEntity.ok("Post submitted for review");
        } catch (Exception e) {
            logger.error("Error while submitting post for review. Post ID: {}", reviewRequest.getPostId(), e);
            return ResponseEntity.internalServerError().body("Failed to submit post for review");
        }
    }

    @GetMapping
    public ResponseEntity<List<ReviewWithPostDetailsDTO>> getAllReviews(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        logger.info("Received request to fetch all reviews with post details. Role: {}", role);

        if (!"editor".equals(role)) {
            logger.warn("Unauthorized attempt to fetch all reviews. Role: {}", role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<ReviewWithPostDetailsDTO> reviews = reviewService.getAllReviewsWithPostDetails();
            logger.info("Successfully fetched {} reviews.", reviews.size());
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error fetching reviews.", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<String> approveReview(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long reviewId,
            @RequestParam String reviewer) {
        logger.info("Received request to approve review. Review ID: {}, Reviewer: {}, Role: {}",
                reviewId, reviewer, role);

        if (!"editor".equals(role)) {
            logger.warn("Unauthorized attempt to approve review. Review ID: {}, Role: {}", reviewId, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            reviewService.approveReview(reviewId, reviewer);
            logger.info("Successfully approved review with ID: {} by Reviewer: {}", reviewId, reviewer);
            return ResponseEntity.ok("Review approved");
        } catch (Exception e) {
            logger.error("Error approving review with ID: {}", reviewId, e);
            return ResponseEntity.internalServerError().body("Failed to approve review");
        }
    }

    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<String> rejectReview(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long reviewId,
            @RequestBody RejectRequest rejectRequest) {
        logger.info("Received request to reject review. Review ID: {}, Reviewer: {}, Remarks: {}, Role: {}",
                reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks(), role);

        if (!"editor".equals(role)) {
            logger.warn("Unauthorized attempt to reject review. Review ID: {}, Role: {}", reviewId, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            reviewService.rejectReview(reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());
            logger.info("Successfully rejected review with ID: {} by Reviewer: {} with Remarks: {}",
                    reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());
            return ResponseEntity.ok("Review rejected with remarks: " + rejectRequest.getRemarks());
        } catch (Exception e) {
            logger.error("Error rejecting review with ID: {}", reviewId, e);
            return ResponseEntity.internalServerError().body("Failed to reject review");
        }
    }

    @GetMapping("/notifications")
    public SseEmitter getNotifications(){
        logger.info("New client connected for notifications");

        try {
            SseEmitter emitter = reviewService.registerClient();
            logger.info("Successfully registered SSE client for notifications.");
            return emitter;
        } catch (Exception e) {
            logger.error("Error registering SSE client for notifications.", e);
            throw e;
        }
    }
}