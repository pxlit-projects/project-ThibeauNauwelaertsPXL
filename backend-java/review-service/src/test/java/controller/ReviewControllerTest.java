package controller;

import org.JavaPE.controller.Request.ReviewRequest;
import org.JavaPE.controller.ReviewController;
import org.JavaPE.controller.dto.RejectRequest;
import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.JavaPE.services.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@WebMvcTest(ReviewController.class)
@ContextConfiguration(classes = {ReviewController.class})
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    void testSubmitPostForReview() throws Exception {
        doNothing().when(reviewService).submitForReview(anyLong(), anyString());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/reviews/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\": 1, \"author\": \"Author\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().string("Post submitted for review"));
    }

    @Test
    void testGetAllReviews() throws Exception {
        when(reviewService.getAllReviewsWithPostDetails()).thenReturn(List.of(new ReviewWithPostDetailsDTO()));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/reviews")
                .header("X-User-Role", "editor"));

        result.andExpect(status().isOk());
    }

    @Test
    void testApproveReview() throws Exception {
        doNothing().when(reviewService).approveReview(anyLong(), anyString());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/reviews/{reviewId}/approve", 1L)
                .header("X-User-Role", "editor")
                .param("reviewer", "John"));

        result.andExpect(status().isOk())
                .andExpect(content().string("Review approved"));
    }

    @Test
    void testRejectReview() throws Exception {
        doNothing().when(reviewService).rejectReview(anyLong(), anyString(), anyString());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/reviews/{reviewId}/reject", 1L)
                .header("X-User-Role", "editor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reviewer\": \"John\", \"remarks\": \"Not good enough\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().string("Review rejected with remarks: Not good enough"));
    }

    @Test
    void testGetNotifications() throws Exception {
        when(reviewService.registerClient()).thenReturn(new SseEmitter());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/reviews/notifications"));

        result.andExpect(status().isOk());
    }
}
