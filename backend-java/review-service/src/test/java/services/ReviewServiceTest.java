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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

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

    @Mock
    private SseEmitter mockEmitter1;

    @Mock
    private SseEmitter mockEmitter2;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;

    @Captor
    private ArgumentCaptor<NotificationMessage> notificationCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Existing tests...

    @Test
    void testSubmitForReview() {
        // Arrange
        Long postId = 1L;
        String author = "Author";

        // Act
        reviewService.submitForReview(postId, author);

        // Assert
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();

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

        verify(rabbitTemplate).convertAndSend(eq("notificationExchange"), eq("notification.key"), notificationCaptor.capture());
        NotificationMessage message = notificationCaptor.getValue();

        assertEquals("approved", message.getStatus());
        assertEquals(reviewer, message.getReviewer());
        assertEquals(2L, message.getPostId());
        assertNull(message.getRemarks());
    }

    @Test
    void testApproveReview_ReviewNotFound() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> reviewService.approveReview(1L, "John"));
        assertEquals("Review not found", exception.getMessage());
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

        verify(rabbitTemplate).convertAndSend(eq("notificationExchange"), eq("notification.key"), notificationCaptor.capture());
        NotificationMessage message = notificationCaptor.getValue();

        assertEquals("rejected", message.getStatus());
        assertEquals(reviewer, message.getReviewer());
        assertEquals(remarks, message.getRemarks());
        assertEquals(2L, message.getPostId());
    }

    @Test
    void testRejectReview_ReviewNotFound() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> reviewService.rejectReview(1L, "Jane", "Invalid content"));
        assertEquals("Review not found", exception.getMessage());
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
        ReviewWithPostDetailsDTO dto = result.get(0);
        assertEquals("Post Title", dto.getPostTitle());
        assertEquals("Post Content", dto.getPostContent());
        assertEquals("Author", dto.getAuthor());
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
        verifyNoInteractions(mockEmitter1, mockEmitter2); // No interactions with mock emitters yet
    }

    // New Test Methods

    /**
     * Tests that hasActiveReviewForPost returns true when a PENDING review exists.
     */
    @Test
    void testHasActiveReviewForPost_WhenExists() {
        // Arrange
        Long postId = 1L;
        when(reviewRepository.existsByPostIdAndStatus(postId, "PENDING")).thenReturn(true);

        // Act
        boolean hasActive = reviewService.hasActiveReviewForPost(postId);

        // Assert
        assertTrue(hasActive);
        verify(reviewRepository).existsByPostIdAndStatus(postId, "PENDING");
    }

    /**
     * Tests that hasActiveReviewForPost returns false when no PENDING review exists.
     */
    @Test
    void testHasActiveReviewForPost_WhenNotExists() {
        // Arrange
        Long postId = 1L;
        when(reviewRepository.existsByPostIdAndStatus(postId, "PENDING")).thenReturn(false);

        // Act
        boolean hasActive = reviewService.hasActiveReviewForPost(postId);

        // Assert
        assertFalse(hasActive);
        verify(reviewRepository).existsByPostIdAndStatus(postId, "PENDING");
    }

    /**
     * Tests that removePendingReviewForPost successfully deletes a PENDING review.
     */
    @Test
    void testRemovePendingReviewForPost_WhenExists() {
        // Arrange
        Long postId = 1L;
        doNothing().when(reviewRepository).deleteByPostIdAndStatus(postId, "PENDING");

        // Act
        reviewService.removePendingReviewForPost(postId);

        // Assert
        verify(reviewRepository).deleteByPostIdAndStatus(postId, "PENDING");
    }

    /**
     * Tests that removePendingReviewForPost behaves correctly when no PENDING review exists.
     * Assuming the repository method does nothing in this case.
     */
    @Test
    void testRemovePendingReviewForPost_WhenNotExists() {
        // Arrange
        Long postId = 1L;
        doNothing().when(reviewRepository).deleteByPostIdAndStatus(postId, "PENDING");

        // Act
        reviewService.removePendingReviewForPost(postId);

        // Assert
        verify(reviewRepository).deleteByPostIdAndStatus(postId, "PENDING");
        // Additional assertions can be added if the method has different behaviors based on existence
    }


    /**
     * Tests that registerClient correctly adds an emitter and handles its completion.
     */
    @Test
    void testRegisterClient_AddsEmitter() {
        // Arrange
        ReviewServiceImpl spyService = Mockito.spy(reviewService);
        SseEmitter newEmitter = new SseEmitter();

        // Act
        SseEmitter registeredEmitter = spyService.registerClient();

        assertNotNull(registeredEmitter);
        verify(spyService).registerClient();
    }

    @Test
    void testGetAllReviewsWithPostDetails_PostClientReturnsNull() {
        // Arrange
        when(reviewRepository.findAll()).thenReturn(List.of(mockReview));

        // Configure mockReview behavior
        when(mockReview.getStatus()).thenReturn("PENDING");
        when(mockReview.getPostId()).thenReturn(2L);
        when(mockReview.getAuthor()).thenReturn("Author");

        // Simulate postClient returning null
        when(postClient.getPostById(2L, "EDITOR")).thenReturn(null);

        // Act
        List<ReviewWithPostDetailsDTO> result = reviewService.getAllReviewsWithPostDetails();

        // Assert
        assertEquals(1, result.size());
        ReviewWithPostDetailsDTO dto = result.get(0);
        assertEquals("Unknown Title", dto.getPostTitle());
        assertEquals("Unknown Content", dto.getPostContent());
        assertEquals("Author", dto.getAuthor());
    }

    @Test
    void testApproveReview_AlreadyApproved() {
        // Arrange
        Long reviewId = 1L;
        String reviewer = "John";

        when(mockReview.getId()).thenReturn(reviewId);
        when(mockReview.getPostId()).thenReturn(2L);
        when(mockReview.getStatus()).thenReturn("APPROVED");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

        reviewService.approveReview(reviewId, reviewer);

        verify(postService).publishPost(2L);
        verify(reviewRepository).delete(mockReview);

        verify(rabbitTemplate).convertAndSend(eq("notificationExchange"), eq("notification.key"), notificationCaptor.capture());
        NotificationMessage message = notificationCaptor.getValue();

        assertEquals("approved", message.getStatus());
        assertEquals(reviewer, message.getReviewer());
        assertEquals(2L, message.getPostId());
        assertNull(message.getRemarks());
    }

}
