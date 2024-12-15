package domain;

import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1L);
        post.setTitle("Test Title");
        post.setContent("This is a test content.");
        post.setAuthor("Test Author");
        post.setCreatedDate(LocalDate.of(2024, 12, 14));
        post.setLastModifiedDate(LocalDate.of(2024, 12, 15));
        post.setStatus(PostStatus.DRAFT);
        post.setRemarks("Initial remarks");
    }

    @Test
    void testPostFields() {
        assertEquals(1L, post.getId());
        assertEquals("Test Title", post.getTitle());
        assertEquals("This is a test content.", post.getContent());
        assertEquals("Test Author", post.getAuthor());
        assertEquals(LocalDate.of(2024, 12, 14), post.getCreatedDate());
        assertEquals(LocalDate.of(2024, 12, 15), post.getLastModifiedDate());
        assertEquals(PostStatus.DRAFT, post.getStatus());
        assertEquals("Initial remarks", post.getRemarks());
    }

    @Test
    void testPostSetters() {
        post.setId(2L);
        post.setTitle("Updated Title");
        post.setContent("Updated content.");
        post.setAuthor("Updated Author");
        post.setCreatedDate(LocalDate.of(2025, 1, 1));
        post.setLastModifiedDate(LocalDate.of(2025, 1, 2));
        post.setStatus(PostStatus.PUBLISHED);
        post.setRemarks("Updated remarks");

        assertEquals(2L, post.getId());
        assertEquals("Updated Title", post.getTitle());
        assertEquals("Updated content.", post.getContent());
        assertEquals("Updated Author", post.getAuthor());
        assertEquals(LocalDate.of(2025, 1, 1), post.getCreatedDate());
        assertEquals(LocalDate.of(2025, 1, 2), post.getLastModifiedDate());
        assertEquals(PostStatus.PUBLISHED, post.getStatus());
        assertEquals("Updated remarks", post.getRemarks());
    }

    @Test
    void testPostStatusEnum() {
        post.setStatus(PostStatus.UNDER_REVIEW);
        assertEquals(PostStatus.UNDER_REVIEW, post.getStatus());

        post.setStatus(PostStatus.PUBLISHED);
        assertEquals(PostStatus.PUBLISHED, post.getStatus());

        post.setStatus(PostStatus.DRAFT);
        assertEquals(PostStatus.DRAFT, post.getStatus());
    }

    @Test
    void testToStringMethod() {
        String postString = post.toString();
        assertTrue(postString.contains("Test Title"));
        assertTrue(postString.contains("Test Author"));
    }

    @Test
    void testEqualsAndHashCode() {
        Post post2 = new Post();
        post2.setId(1L);
        post2.setTitle("Test Title");
        post2.setContent("This is a test content.");
        post2.setAuthor("Test Author");
        post2.setCreatedDate(LocalDate.of(2024, 12, 14));
        post2.setLastModifiedDate(LocalDate.of(2024, 12, 15));
        post2.setStatus(PostStatus.DRAFT);
        post2.setRemarks("Initial remarks");

        assertEquals(post, post2);
        assertEquals(post.hashCode(), post2.hashCode());

        post2.setId(2L);
        assertNotEquals(post, post2);
    }

    @Test
    void testNoArgsConstructor() {
        Post newPost = new Post();
        assertNotNull(newPost);
    }

    @Test
    void testAllArgsConstructor() {
        Post newPost = new Post(
                3L,
                "All Args Title",
                "All Args Content",
                "All Args Author",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1),
                PostStatus.UNDER_REVIEW,
                "All Args Remarks"
        );

        assertEquals(3L, newPost.getId());
        assertEquals("All Args Title", newPost.getTitle());
        assertEquals("All Args Content", newPost.getContent());
        assertEquals("All Args Author", newPost.getAuthor());
        assertEquals(LocalDate.of(2024, 1, 1), newPost.getCreatedDate());
        assertEquals(LocalDate.of(2024, 2, 1), newPost.getLastModifiedDate());
        assertEquals(PostStatus.UNDER_REVIEW, newPost.getStatus());
        assertEquals("All Args Remarks", newPost.getRemarks());
    }
}
