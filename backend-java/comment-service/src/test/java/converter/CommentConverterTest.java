package converter;

import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.controller.converter.CommentConverter;
import org.JavaPE.domain.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentConverterTest {

    private CommentConverter commentConverter;

    @BeforeEach
    void setUp() {
        commentConverter = new CommentConverter();
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        Long id = 1L;
        Long postId = 2L;
        String author = "Test Author";
        String content = "Test Content";
        LocalDateTime createdAt = LocalDateTime.now();

        CommentDTO commentDTO = new CommentDTO(id, postId, author, content, createdAt);

        // Act
        Comment comment = commentConverter.toEntity(commentDTO);

        // Assert
        assertNotNull(comment);
        assertEquals(id, comment.getId());
        assertEquals(postId, comment.getPostId());
        assertEquals(author, comment.getAuthor());
        assertEquals(content, comment.getContent());
        assertEquals(createdAt, comment.getCreatedAt());
    }

    @Test
    void testToEntity_NullValues() {
        // Arrange
        CommentDTO commentDTO = new CommentDTO(null, null, null, null, null);

        // Act
        Comment comment = commentConverter.toEntity(commentDTO);

        // Assert
        assertNotNull(comment);
        assertNull(comment.getId());
        assertNull(comment.getPostId());
        assertNull(comment.getAuthor());
        assertNull(comment.getContent());
        assertNull(comment.getCreatedAt());
    }

    @Test
    void testToDTO_Success() {
        // Arrange
        Long id = 1L;
        Long postId = 2L;
        String author = "Test Author";
        String content = "Test Content";
        LocalDateTime createdAt = LocalDateTime.now();

        Comment comment = new Comment(id, postId, author, content, createdAt);

        // Act
        CommentDTO commentDTO = commentConverter.toDTO(comment);

        // Assert
        assertNotNull(commentDTO);
        assertEquals(id, commentDTO.getId());
        assertEquals(postId, commentDTO.getPostId());
        assertEquals(author, commentDTO.getAuthor());
        assertEquals(content, commentDTO.getContent());
        assertEquals(createdAt, commentDTO.getCreatedAt());
    }

    @Test
    void testToDTO_NullValues() {
        // Arrange
        Comment comment = new Comment(null, null, null, null, null);

        // Act
        CommentDTO commentDTO = commentConverter.toDTO(comment);

        // Assert
        assertNotNull(commentDTO);
        assertNull(commentDTO.getId());
        assertNull(commentDTO.getPostId());
        assertNull(commentDTO.getAuthor());
        assertNull(commentDTO.getContent());
        assertNull(commentDTO.getCreatedAt());
    }
}
