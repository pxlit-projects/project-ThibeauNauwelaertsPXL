package org.JavaPE.services;

import org.JavaPE.client.PostClient;
import org.JavaPE.controller.dto.NotificationMessage;
import org.JavaPE.controller.dto.PostResponse;
import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.JavaPE.domain.Review;
import org.JavaPE.repository.ReviewRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostService postService;
    private final PostClient postClient;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             PostService postService,
                             PostClient postClient,
                             RabbitTemplate rabbitTemplate,
                             ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.postService = postService;
        this.postClient = postClient;
        this.rabbitTemplate = rabbitTemplate;
        this.eventPublisher = eventPublisher;
    }

    // Submit a post for review
    public void submitForReview(Long postId, String author) {
        System.out.println("=== ReviewService: submitForReview() ===");
        System.out.println("postId=" + postId + ", author=" + author);

        Review review = new Review();
        review.setPostId(postId);
        review.setAuthor(author);
        review.setStatus("PENDING");
        review.setSubmittedAt(LocalDateTime.now());
        reviewRepository.save(review);

        System.out.println("Review created => ID=" + review.getId()
                + ", postId=" + postId + ", status=PENDING");
    }

    public boolean hasActiveReviewForPost(Long postId) {
        System.out.println("=== ReviewService: hasActiveReviewForPost() ===");
        boolean exists = reviewRepository.existsByPostIdAndStatus(postId, "PENDING");
        System.out.println("postId=" + postId + ", hasActiveReview=" + exists);
        return exists;
    }

    public void approveReview(Long reviewId, String reviewer) {
        System.out.println("=== ReviewService: approveReview() ===");
        System.out.println("reviewId=" + reviewId + ", reviewer=" + reviewer);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        System.out.println("Approving review => postId=" + review.getPostId());
        postService.publishPost(review.getPostId());

        reviewRepository.delete(review);

        sendNotification(review.getPostId(), "approved", reviewer, null);
    }

    public void rejectReview(Long reviewId, String reviewer, String remarks) {
        System.out.println("=== ReviewService: rejectReview() ===");
        System.out.println("reviewId=" + reviewId
                + ", reviewer=" + reviewer
                + ", remarks=" + remarks);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setStatus("REJECTED");
        review.setReviewer(reviewer);
        review.setRemarks(remarks);
        review.setReviewedAt(LocalDateTime.now());

        reviewRepository.save(review);

        System.out.println("Rejected review => ID=" + review.getId()
                + ", postId=" + review.getPostId()
                + ", remarks=" + review.getRemarks());

        sendNotification(review.getPostId(), "rejected", reviewer, remarks);
    }

    public void removePendingReviewForPost(Long postId) {
        System.out.println("=== ReviewService: removePendingReviewForPost() ===");
        System.out.println("Deleting PENDING review for postId=" + postId);

        reviewRepository.deleteByPostIdAndStatus(postId, "PENDING");
    }

    private void sendNotification(Long postId, String status, String reviewer, String remarks) {
        System.out.println("=== ReviewService: sendNotification() ===");
        System.out.println("postId=" + postId
                + ", status=" + status
                + ", reviewer=" + reviewer
                + ", remarks=" + remarks);

        NotificationMessage message = new NotificationMessage();
        message.setPostId(postId);
        message.setStatus(status);
        message.setReviewer(reviewer);
        message.setRemarks(remarks);

        rabbitTemplate.convertAndSend("notificationExchange", "notification.key", message);
        System.out.println("Notification sent to RabbitMQ: " + message);
    }

    public void publishToSseClients(NotificationMessage message) {
        System.out.println("=== ReviewService: publishToSseClients() ===");
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(message);
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public List<ReviewWithPostDetailsDTO> getAllReviewsWithPostDetails() {
        System.out.println("=== ReviewService: getAllReviewsWithPostDetails() ===");
        List<Review> reviews = reviewRepository.findAll();

        // ignoring REJECTED in final result
        List<Review> filtered = reviews.stream()
                .filter(review -> !"REJECTED".equalsIgnoreCase(review.getStatus()))
                .collect(Collectors.toList());

        System.out.println("Number of non-REJECTED reviews found: " + filtered.size());

        return filtered.stream()
                .map(review -> {
                    PostResponse postResponse = postClient.getPostById(review.getPostId(), "EDITOR");
                    return new ReviewWithPostDetailsDTO(
                            review.getId(),
                            review.getPostId(),
                            review.getStatus(),
                            review.getAuthor(),
                            review.getReviewer(),
                            review.getRemarks(),
                            review.getSubmittedAt() != null ? review.getSubmittedAt().toString() : null,
                            review.getReviewedAt() != null ? review.getReviewedAt().toString() : null,
                            (postResponse != null ? postResponse.getTitle() : "Unknown Title"),
                            (postResponse != null ? postResponse.getContent() : "Unknown Content")
                    );
                })
                .collect(Collectors.toList());
    }

    public SseEmitter registerClient() {
        System.out.println("=== ReviewService: registerClient() ===");
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            System.out.println("SSE client onCompletion => removing emitter");
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            System.out.println("SSE client onTimeout => removing emitter");
            emitters.remove(emitter);
        });
        return emitter;
    }
}
