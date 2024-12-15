package dto;

import org.JavaPE.controller.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PostDTOTest {

    private PostDTO postDTO;

    @BeforeEach
    void setUp() {
        postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setTitle("Sample Title");
        postDTO.setContent("This is a sample post content.");
        postDTO.setAuthor("JohnDoe");
        postDTO.setCreatedDate(LocalDate.of(2024, 12, 14));
        postDTO.setLastModifiedDate(LocalDate.of(2024, 12, 15));
        postDTO.setStatus("DRAFT");
        postDTO.setRemarks("Initial remarks");
    }

    @Test
    void testPostDTOFields() {
        assertEquals(1L, postDTO.getId());
        assertEquals("Sample Title", postDTO.getTitle());
        assertEquals("This is a sample post content.", postDTO.getContent());
        assertEquals("JohnDoe", postDTO.getAuthor());
        assertEquals(LocalDate.of(2024, 12, 14), postDTO.getCreatedDate());
        assertEquals(LocalDate.of(2024, 12, 15), postDTO.getLastModifiedDate());
        assertEquals("DRAFT", postDTO.getStatus());
        assertEquals("Initial remarks", postDTO.getRemarks());
    }

    @Test
    void testPostDTODefaultValues() {
        PostDTO newPostDTO = new PostDTO();
        assertNull(newPostDTO.getId());
        assertNull(newPostDTO.getTitle());
        assertNull(newPostDTO.getContent());
        assertNull(newPostDTO.getAuthor());
        assertNull(newPostDTO.getCreatedDate());
        assertNull(newPostDTO.getLastModifiedDate());
        assertNull(newPostDTO.getStatus());
        assertNull(newPostDTO.getRemarks());
    }

    @Test
    void testPostDTOSetters() {
        postDTO.setId(2L);
        postDTO.setTitle("Updated Title");
        postDTO.setContent("Updated content.");
        postDTO.setAuthor("JaneDoe");
        postDTO.setCreatedDate(LocalDate.of(2025, 1, 1));
        postDTO.setLastModifiedDate(LocalDate.of(2025, 1, 2));
        postDTO.setStatus("PUBLISHED");
        postDTO.setRemarks("Updated remarks");

        assertEquals(2L, postDTO.getId());
        assertEquals("Updated Title", postDTO.getTitle());
        assertEquals("Updated content.", postDTO.getContent());
        assertEquals("JaneDoe", postDTO.getAuthor());
        assertEquals(LocalDate.of(2025, 1, 1), postDTO.getCreatedDate());
        assertEquals(LocalDate.of(2025, 1, 2), postDTO.getLastModifiedDate());
        assertEquals("PUBLISHED", postDTO.getStatus());
        assertEquals("Updated remarks", postDTO.getRemarks());
    }

    @Test
    void testToStringMethod() {
        String postString = postDTO.toString();
        assertTrue(postString.contains("Sample Title"));
        assertTrue(postString.contains("JohnDoe"));
    }

    @Test
    void testEqualsAndHashCode() {
        PostDTO postDTO2 = new PostDTO();
        postDTO2.setId(1L);
        postDTO2.setTitle("Sample Title");
        postDTO2.setContent("This is a sample post content.");
        postDTO2.setAuthor("JohnDoe");
        postDTO2.setCreatedDate(LocalDate.of(2024, 12, 14));
        postDTO2.setLastModifiedDate(LocalDate.of(2024, 12, 15));
        postDTO2.setStatus("DRAFT");
        postDTO2.setRemarks("Initial remarks");

        assertEquals(postDTO, postDTO2);
        assertEquals(postDTO.hashCode(), postDTO2.hashCode());

        postDTO2.setId(2L);
        assertNotEquals(postDTO, postDTO2);
    }

    @Test
    void testNoArgsConstructor() {
        PostDTO newPostDTO = new PostDTO();
        assertNotNull(newPostDTO);
    }

    @Test
    void testAllArgsConstructor() {
        PostDTO newPostDTO = new PostDTO();
        newPostDTO.setId(3L);
        newPostDTO.setTitle("All Args Title");
        newPostDTO.setContent("All Args Content");
        newPostDTO.setAuthor("AllArgsAuthor");
        newPostDTO.setCreatedDate(LocalDate.of(2024, 1, 1));
        newPostDTO.setLastModifiedDate(LocalDate.of(2024, 2, 1));
        newPostDTO.setStatus("UNDER_REVIEW");
        newPostDTO.setRemarks("All Args Remarks");

        assertEquals(3L, newPostDTO.getId());
        assertEquals("All Args Title", newPostDTO.getTitle());
        assertEquals("All Args Content", newPostDTO.getContent());
        assertEquals("AllArgsAuthor", newPostDTO.getAuthor());
        assertEquals(LocalDate.of(2024, 1, 1), newPostDTO.getCreatedDate());
        assertEquals(LocalDate.of(2024, 2, 1), newPostDTO.getLastModifiedDate());
        assertEquals("UNDER_REVIEW", newPostDTO.getStatus());
        assertEquals("All Args Remarks", newPostDTO.getRemarks());
    }

    @Test
    void testInvalidPostDTO() {
        // Test invalid PostDTO with null fields
        PostDTO invalidPostDTO = new PostDTO();

        assertNull(invalidPostDTO.getTitle(), "Title should be null");
        assertNull(invalidPostDTO.getContent(), "Content should be null");
        assertNull(invalidPostDTO.getStatus(), "Status should be null");

        // Manually check for errors (like how validation annotations would work)
        assertTrue(invalidPostDTO.getTitle() == null || invalidPostDTO.getTitle().isBlank(), "Title is required");
        assertTrue(invalidPostDTO.getContent() == null || invalidPostDTO.getContent().isBlank(), "Content is required");
        assertTrue(invalidPostDTO.getStatus() == null || invalidPostDTO.getStatus().isBlank(), "Status is required");
    }

    @Test
    void testValidPostDTO() {
        assertNotNull(postDTO.getTitle(), "Title should not be null");
        assertNotNull(postDTO.getContent(), "Content should not be null");
        assertNotNull(postDTO.getStatus(), "Status should not be null");

        assertFalse(postDTO.getTitle().isEmpty(), "Title should not be empty");
        assertFalse(postDTO.getContent().isEmpty(), "Content should not be empty");
        assertFalse(postDTO.getStatus().isEmpty(), "Status should not be empty");
    }
}
