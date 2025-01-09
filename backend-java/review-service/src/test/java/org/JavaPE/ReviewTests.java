package org.JavaPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.JavaPE.controller.Request.ReviewRequest;
import org.JavaPE.controller.dto.RejectRequest;
import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.JavaPE.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ReviewServiceApplication.class, properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReviewTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Container
    private static MySQLContainer<?> sqlContainer = new MySQLContainer<>("mysql:5.7")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", sqlContainer::getDriverClassName);
    }

    @BeforeEach
    public void setUp() {
        Mockito.reset(reviewService);
    }

    /**
     * Test checking for active review successfully returns true.
     */
    @Test
    public void testHasActiveReviewForPostTrue() throws Exception {
        Long postId = 1L;
        Mockito.when(reviewService.hasActiveReviewForPost(postId)).thenReturn(true);

        mockMvc.perform(get("/reviews/has-active-review")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Mockito.verify(reviewService, Mockito.times(1)).hasActiveReviewForPost(postId);
    }

    /**
     * Test checking for active review successfully returns false.
     */
    @Test
    public void testHasActiveReviewForPostFalse() throws Exception {
        Long postId = 2L;
        Mockito.when(reviewService.hasActiveReviewForPost(postId)).thenReturn(false);

        mockMvc.perform(get("/reviews/has-active-review")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        Mockito.verify(reviewService, Mockito.times(1)).hasActiveReviewForPost(postId);
    }

    /**
     * Test submitting a post for review successfully.
     */
    @Test
    public void testSubmitPostForReviewSuccess() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setPostId(1L);
        reviewRequest.setAuthor("author1");

        Mockito.doNothing().when(reviewService).submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());

        mockMvc.perform(post("/reviews/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Post submitted for review"));

        Mockito.verify(reviewService, Mockito.times(1))
                .submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());
    }

    /**
     * Test submitting a post for review when service throws an exception.
     */
    @Test
    public void testSubmitPostForReviewFailure() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setPostId(1L);
        reviewRequest.setAuthor("author1");

        Mockito.doThrow(new RuntimeException("Submission failed"))
                .when(reviewService).submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());

        mockMvc.perform(post("/reviews/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to submit post for review"));

        Mockito.verify(reviewService, Mockito.times(1))
                .submitForReview(reviewRequest.getPostId(), reviewRequest.getAuthor());
    }

    /**
     * Test fetching all reviews with 'editor' role successfully.
     */
    @Test
    public void testGetAllReviewsWithRoleSuccess() throws Exception {
        ReviewWithPostDetailsDTO review1 = new ReviewWithPostDetailsDTO();
        review1.setReviewId(1L);
        review1.setPostId(1L);
        review1.setPostTitle("Post 1");
        review1.setReviewer("Reviewer1");
        review1.setStatus("APPROVED");

        ReviewWithPostDetailsDTO review2 = new ReviewWithPostDetailsDTO();
        review2.setReviewId(2L);
        review2.setPostId(2L);
        review2.setPostTitle("Post 2");
        review2.setReviewer("Reviewer2");
        review2.setStatus("PENDING");

        List<ReviewWithPostDetailsDTO> reviews = Arrays.asList(review1, review2);

        Mockito.when(reviewService.getAllReviewsWithPostDetails()).thenReturn(reviews);

        String response = mockMvc.perform(get("/reviews")
                        .header("X-User-Role", "editor"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ReviewWithPostDetailsDTO> fetchedReviews = objectMapper.readValue(response, new TypeReference<>() {});
        assert(fetchedReviews.size() == 2);
        assert(fetchedReviews.get(0).getReviewId().equals(1L));
        assert(fetchedReviews.get(1).getReviewId().equals(2L));

        Mockito.verify(reviewService, Mockito.times(1)).getAllReviewsWithPostDetails();
    }

    /**
     * Test fetching all reviews without 'editor' role (should be forbidden).
     */
    @Test
    public void testGetAllReviewsForbidden() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isForbidden());

        Mockito.verify(reviewService, Mockito.never()).getAllReviewsWithPostDetails();
    }

    /**
     * Test approving a review successfully.
     */
    @Test
    public void testApproveReviewSuccess() throws Exception {
        Long reviewId = 1L;
        String reviewer = "Reviewer1";

        Mockito.doNothing().when(reviewService).approveReview(reviewId, reviewer);

        mockMvc.perform(put("/reviews/{reviewId}/approve", reviewId)
                        .header("X-User-Role", "editor")
                        .param("reviewer", reviewer))
                .andExpect(status().isOk())
                .andExpect(content().string("Review approved"));

        Mockito.verify(reviewService, Mockito.times(1)).approveReview(reviewId, reviewer);
    }

    /**
     * Test approving a review without 'editor' role (should be forbidden).
     */
    @Test
    public void testApproveReviewForbidden() throws Exception {
        Long reviewId = 1L;
        String reviewer = "Reviewer1";

        mockMvc.perform(put("/reviews/{reviewId}/approve", reviewId)
                        .header("X-User-Role", "viewer")
                        .param("reviewer", reviewer))
                .andExpect(status().isForbidden());

        Mockito.verify(reviewService, Mockito.never()).approveReview(anyLong(), anyString());
    }

    /**
     * Test approving a non-existing review (should handle exception).
     */
    @Test
    public void testApproveReviewNotFound() throws Exception {
        Long reviewId = 999L;
        String reviewer = "Reviewer1";

        Mockito.doThrow(new RuntimeException("Review not found"))
                .when(reviewService).approveReview(reviewId, reviewer);

        mockMvc.perform(put("/reviews/{reviewId}/approve", reviewId)
                        .header("X-User-Role", "editor")
                        .param("reviewer", reviewer))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to approve review"));

        Mockito.verify(reviewService, Mockito.times(1)).approveReview(reviewId, reviewer);
    }

    /**
     * Test rejecting a review successfully.
     */
    @Test
    public void testRejectReviewSuccess() throws Exception {
        Long reviewId = 1L;
        RejectRequest rejectRequest = new RejectRequest();
        rejectRequest.setReviewer("Reviewer1");
        rejectRequest.setRemarks("Insufficient quality");

        Mockito.doNothing().when(reviewService)
                .rejectReview(reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());

        mockMvc.perform(put("/reviews/{reviewId}/reject", reviewId)
                        .header("X-User-Role", "editor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Review rejected with remarks: " + rejectRequest.getRemarks()));

        Mockito.verify(reviewService, Mockito.times(1))
                .rejectReview(reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());
    }

    /**
     * Test rejecting a review without 'editor' role (should be forbidden).
     */
    @Test
    public void testRejectReviewForbidden() throws Exception {
        Long reviewId = 1L;
        RejectRequest rejectRequest = new RejectRequest();
        rejectRequest.setReviewer("Reviewer1");
        rejectRequest.setRemarks("Insufficient quality");

        mockMvc.perform(put("/reviews/{reviewId}/reject", reviewId)
                        .header("X-User-Role", "viewer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isForbidden());

        Mockito.verify(reviewService, Mockito.never())
                .rejectReview(anyLong(), anyString(), anyString());
    }

    /**
     * Test rejecting a review when service throws an exception.
     */
    @Test
    public void testRejectReviewFailure() throws Exception {
        Long reviewId = 1L;
        RejectRequest rejectRequest = new RejectRequest();
        rejectRequest.setReviewer("Reviewer1");
        rejectRequest.setRemarks("Insufficient quality");

        Mockito.doThrow(new RuntimeException("Rejection failed"))
                .when(reviewService)
                .rejectReview(reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());

        mockMvc.perform(put("/reviews/{reviewId}/reject", reviewId)
                        .header("X-User-Role", "editor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to reject review"));

        Mockito.verify(reviewService, Mockito.times(1))
                .rejectReview(reviewId, rejectRequest.getReviewer(), rejectRequest.getRemarks());
    }

    /**
     * Test fetching all reviews with 'editor' role when no reviews are present.
     */
    @Test
    public void testGetAllReviewsWithRoleEmpty() throws Exception {
        Mockito.when(reviewService.getAllReviewsWithPostDetails()).thenReturn(List.of());

        mockMvc.perform(get("/reviews")
                        .header("X-User-Role", "editor"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(reviewService, Mockito.times(1)).getAllReviewsWithPostDetails();
    }

    /**
     * Test fetching reviews when service throws an exception.
     */
    @Test
    public void testGetAllReviewsServiceException() throws Exception {
        Mockito.when(reviewService.getAllReviewsWithPostDetails())
                .thenThrow(new RuntimeException("Service failure"));

        mockMvc.perform(get("/reviews")
                        .header("X-User-Role", "editor"))
                .andExpect(status().isInternalServerError());

        Mockito.verify(reviewService, Mockito.times(1)).getAllReviewsWithPostDetails();
    }

    /**
     * Test fetching notifications successfully.
     */
    @Test
    public void testGetNotificationsSuccess() throws Exception {
        SseEmitter emitter = new SseEmitter();
        Mockito.when(reviewService.registerClient()).thenReturn(emitter);

        mockMvc.perform(get("/reviews/notifications"))
                .andExpect(status().isOk());

        Mockito.verify(reviewService, Mockito.times(1)).registerClient();
    }
}
