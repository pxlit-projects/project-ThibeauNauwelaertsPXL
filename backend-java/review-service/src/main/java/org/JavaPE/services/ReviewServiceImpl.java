package org.JavaPE.services;

import org.JavaPE.controller.dto.NotificationMessage;
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

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostService postService;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;

    // List to hold all SSE emitters (connected clients)
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public ReviewServiceImpl(ReviewRepository reviewRepository, PostService postService, RabbitTemplate rabbitTemplate, ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.postService = postService;
        this.rabbitTemplate = rabbitTemplate;
        this.eventPublisher = eventPublisher;
    }

    // Submit a post for review
    public void submitForReview(Long postId, String author) {
        Review review = new Review();
        review.setPostId(postId);
        review.setAuthor(author);
        review.setStatus("PENDING");
        review.setSubmittedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    // Approve a review and delegate post publishing to PostService
    public void approveReview(Long reviewId, String reviewer) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Use PostService to publish the post
        postService.publishPost(review.getPostId());

        // Remove the review after approval
        reviewRepository.delete(review);

        sendNotification(review.getPostId(), "approved", reviewer, null);
    }

    // Reject a review
    public void rejectReview(Long reviewId, String reviewer, String remarks) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setStatus("REJECTED");
        review.setReviewer(reviewer);
        review.setRemarks(remarks); // Save the remarks
        review.setReviewedAt(LocalDateTime.now());
        reviewRepository.save(review);

        sendNotification(review.getPostId(), "rejected", reviewer, remarks);
    }

    // Send notification to RabbitMQ and to SSE clients
    private void sendNotification(Long postId, String status, String reviewer, String remarks) {
        NotificationMessage message = new NotificationMessage();
        message.setPostId(postId);
        message.setStatus(status);
        message.setReviewer(reviewer);
        message.setRemarks(remarks);

        // Send to RabbitMQ
        rabbitTemplate.convertAndSend("notificationExchange", "notification.key", message);
        System.out.println("Notification sent to RabbitMQ: " + message);

        // Publish the message to SSE clients
        publishToSseClients(message);
    }

    // Publish notification to SSE clients
    private void publishToSseClients(NotificationMessage message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(message);  // Send the notification to connected clients
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    // Get all reviews
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Register new SSE clients
    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }
}
