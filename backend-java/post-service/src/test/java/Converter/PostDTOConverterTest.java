package Converter;

import org.JavaPE.controller.converter.PostDTOConverter;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PostDTOConverterTest {

    private PostDTOConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PostDTOConverter();
    }

    @Test
    void testConvertToDTO() {
        // Given
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Sample Title");
        post.setContent("This is a sample content.");
        post.setAuthor("JohnDoe");
        post.setCreatedDate(LocalDate.of(2024, 12, 14));
        post.setLastModifiedDate(LocalDate.of(2024, 12, 15));
        post.setStatus(PostStatus.DRAFT);
        post.setRemarks("Some remarks");

        // When
        PostDTO postDTO = converter.convertToDTO(post);

        // Then
        assertNotNull(postDTO);
        assertEquals(1L, postDTO.getId());
        assertEquals("Sample Title", postDTO.getTitle());
        assertEquals("This is a sample content.", postDTO.getContent());
        assertEquals("JohnDoe", postDTO.getAuthor());
        assertEquals(LocalDate.of(2024, 12, 14), postDTO.getCreatedDate());
        assertEquals(LocalDate.of(2024, 12, 15), postDTO.getLastModifiedDate());
        assertEquals("DRAFT", postDTO.getStatus()); // Enum to String conversion check
        assertEquals("Some remarks", postDTO.getRemarks());
    }

    @Test
    void testConvertToEntity() {
        // Given
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Another Title");
        postDTO.setContent("Another content.");
        postDTO.setAuthor("JaneDoe");
        postDTO.setRemarks("Initial remarks");

        // When
        Post post = converter.convertToEntity(postDTO);

        // Then
        assertNotNull(post);
        assertEquals("Another Title", post.getTitle());
        assertEquals("Another content.", post.getContent());
        assertEquals("JaneDoe", post.getAuthor());
        assertEquals(PostStatus.PUBLISHED, post.getStatus()); // Check if status is set to PUBLISHED
        assertEquals(LocalDate.now(), post.getCreatedDate()); // Check if current date is set
        assertEquals(LocalDate.now(), post.getLastModifiedDate()); // Check if current date is set
        assertEquals("Initial remarks", post.getRemarks());
    }

    @Test
    void testConvertToDTOWithNullValues() {
        // Given
        Post post = new Post();
        post.setId(null);
        post.setTitle(null);
        post.setContent(null);
        post.setAuthor(null);
        post.setCreatedDate(null);
        post.setLastModifiedDate(null);
        post.setStatus(null);
        post.setRemarks(null);

        // When
        PostDTO postDTO = converter.convertToDTO(post);

        // Then
        assertNotNull(postDTO);
        assertNull(postDTO.getId());
        assertNull(postDTO.getTitle());
        assertNull(postDTO.getContent());
        assertNull(postDTO.getAuthor());
        assertNull(postDTO.getCreatedDate());
        assertNull(postDTO.getLastModifiedDate());
        assertNull(postDTO.getStatus());
        assertNull(postDTO.getRemarks());
    }

    @Test
    void testConvertToEntityWithNullValues() {
        // Given
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle(null);
        postDTO.setContent(null);
        postDTO.setAuthor(null);
        postDTO.setRemarks(null);

        // When
        Post post = converter.convertToEntity(postDTO);

        // Then
        assertNotNull(post);
        assertNull(post.getTitle());
        assertNull(post.getContent());
        assertNull(post.getAuthor());
        assertEquals(PostStatus.PUBLISHED, post.getStatus()); // Default status should still be PUBLISHED
        assertEquals(LocalDate.now(), post.getCreatedDate()); // Check if current date is set
        assertEquals(LocalDate.now(), post.getLastModifiedDate()); // Check if current date is set
        assertNull(post.getRemarks()); // Remarks is null because postDTO's remarks were null
    }

    @Test
    void testConvertToDTOWithDifferentStatuses() {
        // Given
        Post postDraft = new Post();
        postDraft.setStatus(PostStatus.DRAFT);

        Post postPublished = new Post();
        postPublished.setStatus(PostStatus.PUBLISHED);

        Post postUnderReview = new Post();
        postUnderReview.setStatus(PostStatus.UNDER_REVIEW);

        // When
        PostDTO postDraftDTO = converter.convertToDTO(postDraft);
        PostDTO postPublishedDTO = converter.convertToDTO(postPublished);
        PostDTO postUnderReviewDTO = converter.convertToDTO(postUnderReview);

        // Then
        assertEquals("DRAFT", postDraftDTO.getStatus());
        assertEquals("PUBLISHED", postPublishedDTO.getStatus());
        assertEquals("UNDER_REVIEW", postUnderReviewDTO.getStatus());
    }

    @Test
    void testConvertToEntityWithDateCheck() {
        // Given
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("New Post");
        postDTO.setContent("This is new content");
        postDTO.setAuthor("Author");

        // When
        Post post = converter.convertToEntity(postDTO);

        // Then
        assertEquals(LocalDate.now(), post.getCreatedDate());
        assertEquals(LocalDate.now(), post.getLastModifiedDate());
    }

    @Test
    void testDefaultStatusIsPublishedWhenConvertingToEntity() {
        // Given
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("New Post");
        postDTO.setContent("This is new content");
        postDTO.setAuthor("Author");

        // When
        Post post = converter.convertToEntity(postDTO);

        // Then
        assertEquals(PostStatus.PUBLISHED, post.getStatus());
    }

    @Test
    void testSetStatusProperlyFromDTO() {
        // Given
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Post Title");
        postDTO.setContent("Post Content");
        postDTO.setAuthor("Author");
        postDTO.setStatus("DRAFT");

        // When
        Post post = converter.convertToEntity(postDTO);

        // Then
        assertEquals(PostStatus.PUBLISHED, post.getStatus()); // Status should be PUBLISHED by default
    }
}
