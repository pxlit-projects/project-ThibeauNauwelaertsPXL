package dto;

import org.JavaPE.controller.dto.ReviewWithPostDetailsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewWithPostDetailsDTOTest {

    private ReviewWithPostDetailsDTO reviewWithPostDetailsDTO;

    @BeforeEach
    void setUp() {
        reviewWithPostDetailsDTO = new ReviewWithPostDetailsDTO(
                1L,
                101L,
                "PENDING",
                "JohnDoe",
                "JaneDoe",
                "This is a remark",
                "2024-12-14T10:00:00",
                "2024-12-15T12:00:00",
                "Post Title",
                "Post Content"
        );
    }

    @Test
    void testFields() {
        assertEquals(1L, reviewWithPostDetailsDTO.getReviewId());
        assertEquals(101L, reviewWithPostDetailsDTO.getPostId());
        assertEquals("PENDING", reviewWithPostDetailsDTO.getStatus());
        assertEquals("JohnDoe", reviewWithPostDetailsDTO.getAuthor());
        assertEquals("JaneDoe", reviewWithPostDetailsDTO.getReviewer());
        assertEquals("This is a remark", reviewWithPostDetailsDTO.getRemarks());
        assertEquals("2024-12-14T10:00:00", reviewWithPostDetailsDTO.getSubmittedAt());
        assertEquals("2024-12-15T12:00:00", reviewWithPostDetailsDTO.getReviewedAt());
        assertEquals("Post Title", reviewWithPostDetailsDTO.getPostTitle());
        assertEquals("Post Content", reviewWithPostDetailsDTO.getPostContent());
    }

    @Test
    void testSetters() {
        reviewWithPostDetailsDTO.setReviewId(2L);
        reviewWithPostDetailsDTO.setPostId(202L);
        reviewWithPostDetailsDTO.setStatus("APPROVED");
        reviewWithPostDetailsDTO.setAuthor("Alice");
        reviewWithPostDetailsDTO.setReviewer("Bob");
        reviewWithPostDetailsDTO.setRemarks("New remark");
        reviewWithPostDetailsDTO.setSubmittedAt("2025-01-01T10:00:00");
        reviewWithPostDetailsDTO.setReviewedAt("2025-01-02T12:00:00");
        reviewWithPostDetailsDTO.setPostTitle("New Title");
        reviewWithPostDetailsDTO.setPostContent("New Content");

        assertEquals(2L, reviewWithPostDetailsDTO.getReviewId());
        assertEquals(202L, reviewWithPostDetailsDTO.getPostId());
        assertEquals("APPROVED", reviewWithPostDetailsDTO.getStatus());
        assertEquals("Alice", reviewWithPostDetailsDTO.getAuthor());
        assertEquals("Bob", reviewWithPostDetailsDTO.getReviewer());
        assertEquals("New remark", reviewWithPostDetailsDTO.getRemarks());
        assertEquals("2025-01-01T10:00:00", reviewWithPostDetailsDTO.getSubmittedAt());
        assertEquals("2025-01-02T12:00:00", reviewWithPostDetailsDTO.getReviewedAt());
        assertEquals("New Title", reviewWithPostDetailsDTO.getPostTitle());
        assertEquals("New Content", reviewWithPostDetailsDTO.getPostContent());
    }

    @Test
    void testAllArgsConstructor() {
        ReviewWithPostDetailsDTO newDTO = new ReviewWithPostDetailsDTO(
                3L,
                303L,
                "REJECTED",
                "Charlie",
                "Dave",
                "Rejection reason",
                "2025-01-15T10:00:00",
                "2025-01-16T12:00:00",
                "Another Post Title",
                "Another Post Content"
        );

        assertEquals(3L, newDTO.getReviewId());
        assertEquals(303L, newDTO.getPostId());
        assertEquals("REJECTED", newDTO.getStatus());
        assertEquals("Charlie", newDTO.getAuthor());
        assertEquals("Dave", newDTO.getReviewer());
        assertEquals("Rejection reason", newDTO.getRemarks());
        assertEquals("2025-01-15T10:00:00", newDTO.getSubmittedAt());
        assertEquals("2025-01-16T12:00:00", newDTO.getReviewedAt());
        assertEquals("Another Post Title", newDTO.getPostTitle());
        assertEquals("Another Post Content", newDTO.getPostContent());
    }

    @Test
    void testNoArgsConstructor() {
        ReviewWithPostDetailsDTO emptyDTO = new ReviewWithPostDetailsDTO();

        assertNotNull(emptyDTO);

        emptyDTO.setReviewId(4L);
        emptyDTO.setPostId(404L);
        emptyDTO.setStatus("PENDING");
        emptyDTO.setAuthor("New Author");
        emptyDTO.setReviewer("New Reviewer");
        emptyDTO.setRemarks("Pending review");
        emptyDTO.setSubmittedAt("2026-01-01T10:00:00");
        emptyDTO.setReviewedAt("2026-01-02T12:00:00");
        emptyDTO.setPostTitle("Pending Post Title");
        emptyDTO.setPostContent("Pending Post Content");

        assertEquals(4L, emptyDTO.getReviewId());
        assertEquals(404L, emptyDTO.getPostId());
        assertEquals("PENDING", emptyDTO.getStatus());
        assertEquals("New Author", emptyDTO.getAuthor());
        assertEquals("New Reviewer", emptyDTO.getReviewer());
        assertEquals("Pending review", emptyDTO.getRemarks());
        assertEquals("2026-01-01T10:00:00", emptyDTO.getSubmittedAt());
        assertEquals("2026-01-02T12:00:00", emptyDTO.getReviewedAt());
        assertEquals("Pending Post Title", emptyDTO.getPostTitle());
        assertEquals("Pending Post Content", emptyDTO.getPostContent());
    }
}
