package org.JavaPE.services;

import org.JavaPE.domain.Review;
import org.JavaPE.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public void submitForReview(Long postId, String author) {
        Review review = new Review();
        review.setPostId(postId);
        review.setAuthor(author);
        review.setStatus("PENDING");
        review.setSubmittedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    public void approveReview(Long reviewId, String reviewer) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus("APPROVED");
        review.setReviewer(reviewer);
        review.setReviewedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    public void rejectReview(Long reviewId, String reviewer) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus("REJECTED");
        review.setReviewer(reviewer);
        review.setReviewedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}

