package org.JavaPE.controller.DTO;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PostResponseTest {

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String title = "Test Title";
        String content = "This is a test content.";
        String author = "Test Author";

        // Act
        PostResponse postResponse = new PostResponse(id, title, content, author);

        // Assert
        assertEquals(id, postResponse.getId());
        assertEquals(title, postResponse.getTitle());
        assertEquals(content, postResponse.getContent());
        assertEquals(author, postResponse.getAuthor());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        PostResponse postResponse = new PostResponse(1L, "Title", "Content", "Author");

        // Act
        postResponse.setId(2L);
        postResponse.setTitle("Updated Title");
        postResponse.setContent("Updated Content");
        postResponse.setAuthor("Updated Author");

        // Assert
        assertEquals(2L, postResponse.getId());
        assertEquals("Updated Title", postResponse.getTitle());
        assertEquals("Updated Content", postResponse.getContent());
        assertEquals("Updated Author", postResponse.getAuthor());
    }
}
