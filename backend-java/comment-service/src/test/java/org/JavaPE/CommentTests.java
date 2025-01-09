// CommentTests.java
package org.JavaPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.exception.CommentNotFoundException;
import org.JavaPE.exception.InvalidAuthorException;
import org.JavaPE.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the CommentController.
 */
@SpringBootTest(
        classes = CommentServiceApplication.class, // Replace with the appropriate main application class
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Container
    private static MySQLContainer<?> sqlContainer = new MySQLContainer<>("mysql:5.7")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", sqlContainer::getDriverClassName);
    }

    @BeforeEach
    public void setUp() {
        Mockito.reset(commentService);
    }

    @Test
    public void testAddCommentToPostSuccess() throws Exception {
        Long postId = 1L;
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setAuthor("user1");
        commentDTO.setContent("This is a comment.");
        commentDTO.setPostId(postId);

        CommentDTO savedCommentDTO = new CommentDTO();
        savedCommentDTO.setId(100L);
        savedCommentDTO.setAuthor("user1");
        savedCommentDTO.setContent("This is a comment.");
        savedCommentDTO.setPostId(postId);
        savedCommentDTO.setCreatedAt(LocalDateTime.now());

        Mockito.when(commentService.addCommentToPost(eq(postId), any(CommentDTO.class)))
                .thenReturn(savedCommentDTO);

        mockMvc.perform(post("/comments/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCommentDTO.getId()))
                .andExpect(jsonPath("$.author").value(savedCommentDTO.getAuthor()))
                .andExpect(jsonPath("$.content").value(savedCommentDTO.getContent()))
                .andExpect(jsonPath("$.postId").value(savedCommentDTO.getPostId()))
                .andExpect(jsonPath("$.createdAt").exists());

        Mockito.verify(commentService, Mockito.times(1))
                .addCommentToPost(eq(postId), any(CommentDTO.class));
    }

    @Test
    public void testGetCommentsByPostIdSuccess() throws Exception {
        Long postId = 1L;

        CommentDTO comment1 = new CommentDTO();
        comment1.setId(101L);
        comment1.setAuthor("user1");
        comment1.setContent("First comment.");
        comment1.setPostId(postId);
        comment1.setCreatedAt(LocalDateTime.now());

        CommentDTO comment2 = new CommentDTO();
        comment2.setId(102L);
        comment2.setAuthor("user2");
        comment2.setContent("Second comment.");
        comment2.setPostId(postId);
        comment2.setCreatedAt(LocalDateTime.now());

        List<CommentDTO> comments = Arrays.asList(comment1, comment2);

        Mockito.when(commentService.getCommentsByPostId(postId)).thenReturn(comments);

        String response = mockMvc.perform(get("/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<CommentDTO> fetchedComments = objectMapper.readValue(response, new TypeReference<>() {});
        assert(fetchedComments.size() == 2);
        assert(fetchedComments.get(0).getId().equals(comment1.getId()));
        assert(fetchedComments.get(1).getId().equals(comment2.getId()));

        Mockito.verify(commentService, Mockito.times(1)).getCommentsByPostId(postId);
    }

    @Test
    public void testGetCommentsByPostIdNoComments() throws Exception {
        Long postId = 3L;

        Mockito.when(commentService.getCommentsByPostId(postId)).thenReturn(List.of());

        mockMvc.perform(get("/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        Mockito.verify(commentService, Mockito.times(1)).getCommentsByPostId(postId);
    }

    @Test
    public void testUpdateCommentSuccess() throws Exception {
        Long commentId = 101L;
        CommentDTO updatedCommentDTO = new CommentDTO();
        updatedCommentDTO.setAuthor("user1");
        updatedCommentDTO.setContent("Updated comment content.");

        CommentDTO savedCommentDTO = new CommentDTO();
        savedCommentDTO.setId(commentId);
        savedCommentDTO.setAuthor("user1");
        savedCommentDTO.setContent("Updated comment content.");
        savedCommentDTO.setPostId(1L);
        savedCommentDTO.setCreatedAt(LocalDateTime.now());

        Mockito.when(commentService.updateComment(eq(commentId), any(CommentDTO.class)))
                .thenReturn(savedCommentDTO);

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCommentDTO.getId()))
                .andExpect(jsonPath("$.author").value(savedCommentDTO.getAuthor()))
                .andExpect(jsonPath("$.content").value(savedCommentDTO.getContent()))
                .andExpect(jsonPath("$.postId").value(savedCommentDTO.getPostId()))
                .andExpect(jsonPath("$.createdAt").exists());

        Mockito.verify(commentService, Mockito.times(1))
                .updateComment(eq(commentId), any(CommentDTO.class));
    }

    @Test
    public void testUpdateCommentNotFound() throws Exception {
        Long commentId = 999L;
        CommentDTO updatedCommentDTO = new CommentDTO();
        updatedCommentDTO.setAuthor("user1");
        updatedCommentDTO.setContent("Attempting to update a non-existent comment.");

        Mockito.when(commentService.updateComment(eq(commentId), any(CommentDTO.class)))
                .thenThrow(new CommentNotFoundException("Comment not found"));

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCommentDTO)))
                .andExpect(status().isNotFound());
        Mockito.verify(commentService, Mockito.times(1))
                .updateComment(eq(commentId), any(CommentDTO.class));
    }

    @Test
    public void testUpdateCommentUnauthorized() throws Exception {
        Long commentId = 102L;
        CommentDTO updatedCommentDTO = new CommentDTO();
        updatedCommentDTO.setAuthor("user3"); // Different author
        updatedCommentDTO.setContent("Attempting to update someone else's comment.");

        Mockito.when(commentService.updateComment(eq(commentId), any(CommentDTO.class)))
                .thenThrow(new InvalidAuthorException("You are not authorized to update this comment"));

        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCommentDTO)))
                .andExpect(status().isUnauthorized());
        Mockito.verify(commentService, Mockito.times(1))
                .updateComment(eq(commentId), any(CommentDTO.class));
    }

    @Test
    public void testDeleteCommentSuccess() throws Exception {
        Long commentId = 103L;
        String currentUser = "user4";

        Mockito.doNothing().when(commentService).deleteComment(commentId, currentUser);

        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .header("X-User-Role", currentUser))
                .andExpect(status().isNoContent());

        Mockito.verify(commentService, Mockito.times(1))
                .deleteComment(commentId, currentUser);
    }

    @Test
    public void testDeleteCommentNotFound() throws Exception {
        Long commentId = 1000L;
        String currentUser = "user5";

        Mockito.doThrow(new CommentNotFoundException("Comment not found"))
                .when(commentService).deleteComment(commentId, currentUser);

        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .header("X-User-Role", currentUser))
                .andExpect(status().isNotFound());
        Mockito.verify(commentService, Mockito.times(1))
                .deleteComment(commentId, currentUser);
    }

    @Test
    public void testDeleteCommentUnauthorized() throws Exception {
        Long commentId = 104L;
        String currentUser = "user6";

        Mockito.doThrow(new InvalidAuthorException("You are not authorized to delete this comment"))
                .when(commentService).deleteComment(commentId, currentUser);

        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .header("X-User-Role", currentUser))
                .andExpect(status().isUnauthorized());
        Mockito.verify(commentService, Mockito.times(1))
                .deleteComment(commentId, currentUser);
    }

    @Test
    public void testEditCommentSuccess() throws Exception {
        Long commentId = 105L;
        String currentUser = "user7";

        CommentDTO editCommentDTO = new CommentDTO();
        editCommentDTO.setContent("Edited comment content.");

        CommentDTO updatedCommentDTO = new CommentDTO();
        updatedCommentDTO.setId(commentId);
        updatedCommentDTO.setAuthor(currentUser);
        updatedCommentDTO.setContent("Edited comment content.");
        updatedCommentDTO.setPostId(1L);
        updatedCommentDTO.setCreatedAt(LocalDateTime.now());

        Mockito.when(commentService.editComment(eq(commentId), eq(currentUser), any(CommentDTO.class)))
                .thenReturn(updatedCommentDTO);

        mockMvc.perform(put("/comments/{commentId}/edit", commentId)
                        .header("X-User-Role", currentUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedCommentDTO.getId()))
                .andExpect(jsonPath("$.author").value(updatedCommentDTO.getAuthor()))
                .andExpect(jsonPath("$.content").value(updatedCommentDTO.getContent()))
                .andExpect(jsonPath("$.postId").value(updatedCommentDTO.getPostId()))
                .andExpect(jsonPath("$.createdAt").exists());

        Mockito.verify(commentService, Mockito.times(1))
                .editComment(eq(commentId), eq(currentUser), any(CommentDTO.class));
    }

    @Test
    public void testEditCommentNotFound() throws Exception {
        Long commentId = 2000L;
        String currentUser = "user8";

        CommentDTO editCommentDTO = new CommentDTO();
        editCommentDTO.setContent("Attempting to edit a non-existent comment.");

        Mockito.when(commentService.editComment(eq(commentId), eq(currentUser), any(CommentDTO.class)))
                .thenThrow(new CommentNotFoundException("Comment not found"));

        mockMvc.perform(put("/comments/{commentId}/edit", commentId)
                        .header("X-User-Role", currentUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editCommentDTO)))
                .andExpect(status().isNotFound());
        Mockito.verify(commentService, Mockito.times(1))
                .editComment(eq(commentId), eq(currentUser), any(CommentDTO.class));
    }

    @Test
    public void testEditCommentUnauthorized() throws Exception {
        Long commentId = 106L;
        String currentUser = "user9";

        CommentDTO editCommentDTO = new CommentDTO();
        editCommentDTO.setContent("Attempting to edit someone else's comment.");

        Mockito.when(commentService.editComment(eq(commentId), eq(currentUser), any(CommentDTO.class)))
                .thenThrow(new InvalidAuthorException("You are not authorized to edit this comment"));

        mockMvc.perform(put("/comments/{commentId}/edit", commentId)
                        .header("X-User-Role", currentUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editCommentDTO)))
                .andExpect(status().isUnauthorized());
        Mockito.verify(commentService, Mockito.times(1))
                .editComment(eq(commentId), eq(currentUser), any(CommentDTO.class));
    }

    @Test
    public void testGetCommentsByPostIdWithComments() throws Exception {
        Long postId = 1L;

        CommentDTO comment1 = new CommentDTO();
        comment1.setId(201L);
        comment1.setAuthor("user10");
        comment1.setContent("Great post!");
        comment1.setPostId(postId);
        comment1.setCreatedAt(LocalDateTime.now());

        CommentDTO comment2 = new CommentDTO();
        comment2.setId(202L);
        comment2.setAuthor("user11");
        comment2.setContent("Very informative.");
        comment2.setPostId(postId);
        comment2.setCreatedAt(LocalDateTime.now());

        List<CommentDTO> comments = Arrays.asList(comment1, comment2);

        Mockito.when(commentService.getCommentsByPostId(postId)).thenReturn(comments);

        String response = mockMvc.perform(get("/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<CommentDTO> fetchedComments = objectMapper.readValue(response, new TypeReference<>() {});
        assert(fetchedComments.size() == 2);
        assert(fetchedComments.get(0).getId().equals(comment1.getId()));
        assert(fetchedComments.get(1).getId().equals(comment2.getId()));

        Mockito.verify(commentService, Mockito.times(1)).getCommentsByPostId(postId);
    }

}
