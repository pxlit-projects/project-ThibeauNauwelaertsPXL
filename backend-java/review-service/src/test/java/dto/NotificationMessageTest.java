package dto;

import org.JavaPE.controller.dto.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMessageTest {

    private NotificationMessage notificationMessage;

    @BeforeEach
    void setUp() {
        notificationMessage = new NotificationMessage();
        notificationMessage.setPostId(1L);
        notificationMessage.setStatus("APPROVED");
        notificationMessage.setReviewer("JohnDoe");
        notificationMessage.setRemarks("Review completed successfully.");
    }

    @Test
    void testFields() {
        assertEquals(1L, notificationMessage.getPostId());
        assertEquals("APPROVED", notificationMessage.getStatus());
        assertEquals("JohnDoe", notificationMessage.getReviewer());
        assertEquals("Review completed successfully.", notificationMessage.getRemarks());
    }

    @Test
    void testSetters() {
        notificationMessage.setPostId(2L);
        notificationMessage.setStatus("REJECTED");
        notificationMessage.setReviewer("JaneDoe");
        notificationMessage.setRemarks("Review failed due to missing content.");

        assertEquals(2L, notificationMessage.getPostId());
        assertEquals("REJECTED", notificationMessage.getStatus());
        assertEquals("JaneDoe", notificationMessage.getReviewer());
        assertEquals("Review failed due to missing content.", notificationMessage.getRemarks());
    }

    @Test
    void testToString() {
        String toStringValue = notificationMessage.toString();
        assertTrue(toStringValue.contains("postId=1"));
        assertTrue(toStringValue.contains("status='APPROVED'"));
        assertTrue(toStringValue.contains("reviewer='JohnDoe'"));
        assertTrue(toStringValue.contains("remarks='Review completed successfully.'"));
    }

    @Test
    void testNoArgsConstructor() {
        NotificationMessage newMessage = new NotificationMessage();
        assertNotNull(newMessage);
        assertNull(newMessage.getPostId());
        assertNull(newMessage.getStatus());
        assertNull(newMessage.getReviewer());
        assertNull(newMessage.getRemarks());
    }

    @Test
    void testAllArgsConstructor() {
        NotificationMessage newMessage = new NotificationMessage();
        newMessage.setPostId(3L);
        newMessage.setStatus("PENDING");
        newMessage.setReviewer("AllArgsReviewer");
        newMessage.setRemarks("Pending review for the submitted post.");

        assertEquals(3L, newMessage.getPostId());
        assertEquals("PENDING", newMessage.getStatus());
        assertEquals("AllArgsReviewer", newMessage.getReviewer());
        assertEquals("Pending review for the submitted post.", newMessage.getRemarks());
    }
}