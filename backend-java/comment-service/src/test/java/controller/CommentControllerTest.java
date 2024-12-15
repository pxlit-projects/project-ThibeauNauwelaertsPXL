package controller;

import org.JavaPE.controller.CommentController;
import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentService commentService;

    private CommentDTO commentDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();

        commentDTO = new CommentDTO(1L, 1L, "Author", "Test Content", LocalDateTime.now());
    }

    @Test
    void testAddCommentToPost_Success() throws Exception {
        // Arrange
        Long postId = 1L;
        when(commentService.addCommentToPost(eq(postId), any(CommentDTO.class))).thenReturn(commentDTO);

        // Act
        ResultActions result = mockMvc.perform(post("/comments/post/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\": \"Author\", \"content\": \"Test Content\"}"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDTO.getId()))
                .andExpect(jsonPath("$.postId").value(commentDTO.getPostId()))
                .andExpect(jsonPath("$.author").value(commentDTO.getAuthor()))
                .andExpect(jsonPath("$.content").value(commentDTO.getContent()));

        verify(commentService).addCommentToPost(eq(postId), any(CommentDTO.class));
    }

    @Test
    void testGetCommentsByPostId_Success() throws Exception {
        // Arrange
        Long postId = 1L;
        when(commentService.getCommentsByPostId(postId)).thenReturn(List.of(commentDTO));

        // Act
        ResultActions result = mockMvc.perform(get("/comments/post/{postId}", postId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentDTO.getId()))
                .andExpect(jsonPath("$[0].postId").value(commentDTO.getPostId()))
                .andExpect(jsonPath("$[0].author").value(commentDTO.getAuthor()))
                .andExpect(jsonPath("$[0].content").value(commentDTO.getContent()));

        verify(commentService).getCommentsByPostId(postId);
    }

    @Test
    void testUpdateComment_Success() throws Exception {
        // Arrange
        Long commentId = 1L;
        when(commentService.updateComment(eq(commentId), any(CommentDTO.class))).thenReturn(commentDTO);

        // Act
        ResultActions result = mockMvc.perform(put("/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\": \"Author\", \"content\": \"Updated Content\"}"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDTO.getId()))
                .andExpect(jsonPath("$.postId").value(commentDTO.getPostId()))
                .andExpect(jsonPath("$.author").value(commentDTO.getAuthor()))
                .andExpect(jsonPath("$.content").value(commentDTO.getContent()));

        verify(commentService).updateComment(eq(commentId), any(CommentDTO.class));
    }

    @Test
    void testDeleteComment_Success() throws Exception {
        // Arrange
        Long commentId = 1L;
        String currentUser = "Author";

        // Act
        ResultActions result = mockMvc.perform(delete("/comments/{commentId}", commentId)
                .header("X-User-Role", currentUser));

        // Assert
        result.andExpect(status().isNoContent());

        verify(commentService).deleteComment(commentId, currentUser);
    }

    @Test
    void testEditComment_Success() throws Exception {
        // Arrange
        Long commentId = 1L;
        String currentUser = "Author";
        when(commentService.editComment(eq(commentId), eq(currentUser), any(CommentDTO.class))).thenReturn(commentDTO);

        // Act
        ResultActions result = mockMvc.perform(put("/comments/{commentId}/edit", commentId)
                .header("X-User-Role", currentUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\": \"Author\", \"content\": \"Edited Content\"}"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDTO.getId()))
                .andExpect(jsonPath("$.postId").value(commentDTO.getPostId()))
                .andExpect(jsonPath("$.author").value(commentDTO.getAuthor()))
                .andExpect(jsonPath("$.content").value(commentDTO.getContent()));

        verify(commentService).editComment(eq(commentId), eq(currentUser), any(CommentDTO.class));
    }
}
