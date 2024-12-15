package dto;

import org.JavaPE.controller.dto.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostResponseTest {

    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        postResponse = new PostResponse(1L, "Test Title", "Test Content", "JohnDoe");
    }

    @Test
    void testFields() {
        assertEquals(1L, postResponse.getId());
        assertEquals("Test Title", postResponse.getTitle());
        assertEquals("Test Content", postResponse.getContent());
        assertEquals("JohnDoe", postResponse.getAuthor());
    }

    @Test
    void testSetters() {
        postResponse.setId(2L);
        postResponse.setTitle("Updated Title");
        postResponse.setContent("Updated Content");
        postResponse.setAuthor("JaneDoe");

        assertEquals(2L, postResponse.getId());
        assertEquals("Updated Title", postResponse.getTitle());
        assertEquals("Updated Content", postResponse.getContent());
        assertEquals("JaneDoe", postResponse.getAuthor());
    }

    @Test
    void testAllArgsConstructor() {
        PostResponse newResponse = new PostResponse(3L, "New Title", "New Content", "NewAuthor");

        assertEquals(3L, newResponse.getId());
        assertEquals("New Title", newResponse.getTitle());
        assertEquals("New Content", newResponse.getContent());
        assertEquals("NewAuthor", newResponse.getAuthor());
    }
}