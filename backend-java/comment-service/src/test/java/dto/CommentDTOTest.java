package dto;

import org.JavaPE.controller.DTO.CommentDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class CommentDTOTest {

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        Long postId = 2L;
        String author = "Test Author";
        String content = "This is a test comment.";
        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        CommentDTO commentDTO = new CommentDTO(id, postId, author, content, createdAt);

        // Assert
        assertEquals(id, commentDTO.getId());
        assertEquals(postId, commentDTO.getPostId());
        assertEquals(author, commentDTO.getAuthor());
        assertEquals(content, commentDTO.getContent());
        assertEquals(createdAt, commentDTO.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        CommentDTO commentDTO = new CommentDTO(1L, 2L, "Original Author", "Original Content", LocalDateTime.now());

        // Act
        LocalDateTime updatedTime = LocalDateTime.now();
        commentDTO.setId(10L);
        commentDTO.setPostId(20L);
        commentDTO.setAuthor("Updated Author");
        commentDTO.setContent("Updated Content");
        commentDTO.setCreatedAt(updatedTime);

        // Assert
        assertEquals(10L, commentDTO.getId());
        assertEquals(20L, commentDTO.getPostId());
        assertEquals("Updated Author", commentDTO.getAuthor());
        assertEquals("Updated Content", commentDTO.getContent());
        assertEquals(updatedTime, commentDTO.getCreatedAt());
    }
}
