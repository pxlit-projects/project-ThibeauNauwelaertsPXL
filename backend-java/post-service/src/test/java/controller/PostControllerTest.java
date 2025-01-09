package controller;

import org.JavaPE.controller.PostController;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@ContextConfiguration(classes = {PostController.class})
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    private PostDTO postDTO;

    @BeforeEach
    void setUp() {
        postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setStatus("DRAFT");
        postDTO.setTitle("Sample Title");
        postDTO.setContent("Sample Content");
        // Initialize other fields if necessary
    }

    @Test
    void testCreatePost_Success() throws Exception {
        // Arrange
        when(postService.createOrUpdateDraft(any(PostDTO.class))).thenReturn(postDTO);

        String postContent = "{\"title\": \"Sample Title\", \"content\": \"Sample Content\"}";

        // Act
        ResultActions result = mockMvc.perform(post("/posts")
                .header("X-User-Role", "editor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postContent));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(postDTO.getId()))
                .andExpect(jsonPath("$.status").value(postDTO.getStatus()))
                .andExpect(jsonPath("$.title").value(postDTO.getTitle()))
                .andExpect(jsonPath("$.content").value(postDTO.getContent()));

        verify(postService, times(1)).createOrUpdateDraft(any(PostDTO.class));
    }

    @Test
    void testCreatePost_Unauthorized() throws Exception {
        // Arrange
        String postContent = "{\"title\": \"Unauthorized Title\", \"content\": \"Some Content\"}";

        // Act
        ResultActions result = mockMvc.perform(post("/posts")
                .header("X-User-Role", "viewer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postContent));

        // Assert
        result.andExpect(status().isForbidden());
        verify(postService, never()).createOrUpdateDraft(any(PostDTO.class));
    }

    @Test
    void testUpdatePost_Unauthorized() throws Exception {
        // Arrange
        String updateContent = "{\"title\": \"Updated Title\", \"content\": \"Updated Content\"}";

        // Act
        ResultActions result = mockMvc.perform(put("/posts/{postId}", 1L)
                .header("X-User-Role", "viewer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateContent));

        // Assert
        result.andExpect(status().isForbidden());
        verify(postService, never()).editPost(eq(1L), any(PostDTO.class));
        verify(postService, never()).sendForReview(any(PostDTO.class));
    }

    @Test
    void testGetPublishedPosts_Success() throws Exception {
        // Arrange
        List<PostDTO> publishedPosts = Arrays.asList(postDTO, postDTO);
        when(postService.getPublishedPosts()).thenReturn(publishedPosts);

        // Act
        ResultActions result = mockMvc.perform(get("/posts/published")
                .header("X-User-Role", "editor"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(publishedPosts.size()))
                .andExpect(jsonPath("$[0].id").value(postDTO.getId()))
                .andExpect(jsonPath("$[0].status").value(postDTO.getStatus()))
                .andExpect(jsonPath("$[1].id").value(postDTO.getId()))
                .andExpect(jsonPath("$[1].status").value(postDTO.getStatus()));

        verify(postService, times(1)).getPublishedPosts();
    }

    @Test
    void testGetPublishedPosts_Unauthorized() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/posts/published"));

        // Assert
        result.andExpect(status().isForbidden());
        verify(postService, never()).getPublishedPosts();
    }

    @Test
    void testGetDraftPosts_Success() throws Exception {
        // Arrange
        List<PostDTO> draftPosts = Arrays.asList(postDTO, postDTO);
        when(postService.getDraftPosts()).thenReturn(draftPosts);

        // Act
        ResultActions result = mockMvc.perform(get("/posts/drafts")
                .header("X-User-Role", "editor"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(draftPosts.size()))
                .andExpect(jsonPath("$[0].id").value(postDTO.getId()))
                .andExpect(jsonPath("$[0].status").value(postDTO.getStatus()))
                .andExpect(jsonPath("$[1].id").value(postDTO.getId()))
                .andExpect(jsonPath("$[1].status").value(postDTO.getStatus()));

        verify(postService, times(1)).getDraftPosts();
    }

    @Test
    void testGetDraftPosts_Unauthorized() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/posts/drafts"));

        // Assert
        result.andExpect(status().isForbidden());
        verify(postService, never()).getDraftPosts();
    }

    @Test
    void testGetPublishedPostById_Success() throws Exception {
        // Arrange
        when(postService.getPublishedPostById(1L)).thenReturn(postDTO);

        // Act
        ResultActions result = mockMvc.perform(get("/posts/published/{postId}", 1L));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postDTO.getId()))
                .andExpect(jsonPath("$.status").value(postDTO.getStatus()))
                .andExpect(jsonPath("$.title").value(postDTO.getTitle()))
                .andExpect(jsonPath("$.content").value(postDTO.getContent()));

        verify(postService, times(1)).getPublishedPostById(1L);
    }

    @Test
    void testGetPostById_Success() throws Exception {
        // Arrange
        when(postService.getPostById(1L)).thenReturn(postDTO);

        // Act
        ResultActions result = mockMvc.perform(get("/posts/{postId}", 1L)
                .header("X-User-Role", "editor"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postDTO.getId()))
                .andExpect(jsonPath("$.status").value(postDTO.getStatus()))
                .andExpect(jsonPath("$.title").value(postDTO.getTitle()))
                .andExpect(jsonPath("$.content").value(postDTO.getContent()));

        verify(postService, times(1)).getPostById(1L);
    }

    @Test
    void testGetPostById_Unauthorized() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/posts/{postId}", 1L));

        // Assert
        result.andExpect(status().isForbidden());
        verify(postService, never()).getPostById(1L);
    }
}
