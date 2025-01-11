package services;

import org.JavaPE.client.PostClient;
import org.JavaPE.controller.dto.NotificationMessage;
import org.JavaPE.controller.dto.PostResponse;
import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.JavaPE.domain.Review;
import org.JavaPE.repository.ReviewRepository;
import org.JavaPE.services.PostService;
import org.JavaPE.services.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PostService postService;

    @Mock
    private PostClient postClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Review mockReview; // Mocked Review instance

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitForReview() {
        // Arrange
        Long postId = 1L;
        String author = "Author";

        // Act
        reviewService.submitForReview(postId, author);

        // Assert
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review savedReview = captor.getValue();

        assertEquals(postId, savedReview.getPostId());
        assertEquals(author, savedReview.getAuthor());
        assertEquals("PENDING", savedReview.getStatus());
        assertNotNull(savedReview.getSubmittedAt());
    }

    @Test
    void testApproveReview() {
        // Arrange
        Long reviewId = 1L;
        String reviewer = "John";

        // Configure mockReview behavior
        when(mockReview.getId()).thenReturn(reviewId);
        when(mockReview.getPostId()).thenReturn(2L);
        when(mockReview.getStatus()).thenReturn("PENDING");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

        // Act
        reviewService.approveReview(reviewId, reviewer);

        // Assert
        verify(postService).publishPost(2L);
        verify(reviewRepository).delete(mockReview);

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(rabbitTemplate).convertAndSend(eq("notificationExchange"), eq("notification.key"), captor.capture());
        NotificationMessage message = captor.getValue();

        assertEquals("approved", message.getStatus());
        assertEquals(reviewer, message.getReviewer());
        assertEquals(2L, message.getPostId());
    }

    @Test
    void testApproveReview_ReviewNotFound() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> reviewService.approveReview(1L, "John"));
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void testRejectReview() {
        // Arrange
        Long reviewId = 1L;
        String reviewer = "Jane";
        String remarks = "Not good enough";

        // Configure mockReview behavior
        when(mockReview.getId()).thenReturn(reviewId);
        when(mockReview.getPostId()).thenReturn(2L);
        when(mockReview.getStatus()).thenReturn("PENDING");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

        // Act
        reviewService.rejectReview(reviewId, reviewer, remarks);

        // Assert
        // Verify setters are called on mockReview
        verify(mockReview).setStatus("REJECTED");
        verify(mockReview).setReviewer(reviewer);
        verify(mockReview).setRemarks(remarks);
        verify(mockReview).setReviewedAt(any(LocalDateTime.class));

        verify(reviewRepository).save(mockReview);

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(rabbitTemplate).convertAndSend(eq("notificationExchange"), eq("notification.key"), captor.capture());
        NotificationMessage message = captor.getValue();

        assertEquals("rejected", message.getStatus());
        assertEquals(reviewer, message.getReviewer());
        assertEquals(remarks, message.getRemarks());
    }

    @Test
    void testRejectReview_ReviewNotFound() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> reviewService.rejectReview(1L, "Jane", "Invalid content"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetAllReviewsWithPostDetails() {
        // Arrange
        when(reviewRepository.findAll()).thenReturn(List.of(mockReview));

        // Configure mockReview behavior
        when(mockReview.getStatus()).thenReturn("PENDING");
        when(mockReview.getPostId()).thenReturn(2L);
        when(mockReview.getAuthor()).thenReturn("Author");

        PostResponse postResponse = new PostResponse(2L, "Post Title", "Post Content", "Author");
        when(postClient.getPostById(2L, "EDITOR")).thenReturn(postResponse);

        // Act
        List<ReviewWithPostDetailsDTO> result = reviewService.getAllReviewsWithPostDetails();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Post Title", result.get(0).getPostTitle());
        assertEquals("Post Content", result.get(0).getPostContent());
        assertEquals("Author", result.get(0).getAuthor());
    }

    @Test
    void testGetAllReviewsWithPostDetails_RejectedFilteredOut() {
        // Arrange
        when(reviewRepository.findAll()).thenReturn(List.of(mockReview));

        // Configure mockReview behavior
        when(mockReview.getStatus()).thenReturn("REJECTED");

        // Act
        List<ReviewWithPostDetailsDTO> result = reviewService.getAllReviewsWithPostDetails();

        // Assert
        assertEquals(0, result.size()); // Rejected reviews are filtered out
    }

    @Test
    void testRegisterClient() {
        // Act
        SseEmitter emitter = reviewService.registerClient();

        // Assert
        assertNotNull(emitter);
    }
}
