package domain;

import org.JavaPE.domain.Comment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class CommentTest {

    @Test
    void testCreateComment_Success() {
        // Arrange
        Long postId = 1L;
        String author = "Test Author";
        String content = "This is a test comment.";

        // Act
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        // Assert
        assertEquals(postId, comment.getPostId());
        assertEquals(author, comment.getAuthor());
        assertEquals(content, comment.getContent());
        assertNotNull(comment.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAt() {
        // Arrange
        Comment comment = new Comment();

        // Act
        LocalDateTime createdAt = comment.getCreatedAt();

        // Assert
        assertNotNull(createdAt);
        assertTrue(createdAt.isBefore(LocalDateTime.now()) || createdAt.isEqual(LocalDateTime.now()));
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        Comment comment = new Comment();

        // Act
        comment.setId(1L);
        comment.setPostId(2L);
        comment.setAuthor("Updated Author");
        comment.setContent("Updated content");
        LocalDateTime updatedTime = LocalDateTime.now();
        comment.setCreatedAt(updatedTime);

        // Assert
        assertEquals(1L, comment.getId());
        assertEquals(2L, comment.getPostId());
        assertEquals("Updated Author", comment.getAuthor());
        assertEquals("Updated content", comment.getContent());
        assertEquals(updatedTime, comment.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        // Act
        Comment comment = new Comment();

        // Assert
        assertNotNull(comment);
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        Long postId = 2L;
        String author = "Author";
        String content = "This is content.";
        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        Comment comment = new Comment(id, postId, author, content, createdAt);

        // Assert
        assertEquals(id, comment.getId());
        assertEquals(postId, comment.getPostId());
        assertEquals(author, comment.getAuthor());
        assertEquals(content, comment.getContent());
        assertEquals(createdAt, comment.getCreatedAt());
    }
}
