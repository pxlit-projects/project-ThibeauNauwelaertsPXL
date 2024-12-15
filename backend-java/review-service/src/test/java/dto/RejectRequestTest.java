package dto;

import org.JavaPE.controller.dto.RejectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RejectRequestTest {

    private RejectRequest rejectRequest;

    @BeforeEach
    void setUp() {
        rejectRequest = new RejectRequest();
        rejectRequest.setReviewer("JohnDoe");
        rejectRequest.setRemarks("The post content is not sufficient.");
    }

    @Test
    void testFields() {
        assertEquals("JohnDoe", rejectRequest.getReviewer());
        assertEquals("The post content is not sufficient.", rejectRequest.getRemarks());
    }

    @Test
    void testSetters() {
        rejectRequest.setReviewer("JaneDoe");
        rejectRequest.setRemarks("Content contains inappropriate language.");

        assertEquals("JaneDoe", rejectRequest.getReviewer());
        assertEquals("Content contains inappropriate language.", rejectRequest.getRemarks());
    }

    @Test
    void testNoArgsConstructor() {
        RejectRequest newRequest = new RejectRequest();
        assertNotNull(newRequest);
        assertNull(newRequest.getReviewer());
        assertNull(newRequest.getRemarks());
    }

    @Test
    void testAllArgsConstructor() {
        RejectRequest newRequest = new RejectRequest();
        newRequest.setReviewer("AllArgsReviewer");
        newRequest.setRemarks("This is a rejection request for testing purposes.");

        assertEquals("AllArgsReviewer", newRequest.getReviewer());
        assertEquals("This is a rejection request for testing purposes.", newRequest.getRemarks());
    }

}
