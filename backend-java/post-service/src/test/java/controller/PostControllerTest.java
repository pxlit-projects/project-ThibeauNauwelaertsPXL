package controller;

import org.JavaPE.controller.PostController;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.exception.PostNotFoundException;
import org.JavaPE.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePost_Success() {
        PostDTO postDTO = mock(PostDTO.class);
        PostDTO responseDTO = mock(PostDTO.class);

        when(postService.createOrUpdateDraft(postDTO)).thenReturn(responseDTO);

        ResponseEntity<PostDTO> response = postController.createPost("editor", postDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(postService).createOrUpdateDraft(postDTO);
    }

    @Test
    void testCreatePost_Unauthorized() {
        PostDTO postDTO = mock(PostDTO.class);

        ResponseEntity<PostDTO> response = postController.createPost("viewer", postDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(postService, never()).createOrUpdateDraft(postDTO);
    }

    @Test
    void testUpdatePost_Success() {
        PostDTO postDTO = mock(PostDTO.class);
        PostDTO updatedPostDTO = mock(PostDTO.class);

        when(updatedPostDTO.getStatus()).thenReturn("DRAFT");
        when(postService.editPost(1L, postDTO)).thenReturn(updatedPostDTO);

        ResponseEntity<PostDTO> response = postController.updatePost("editor", 1L, postDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedPostDTO, response.getBody());

        verify(postService).editPost(1L, postDTO);
        verify(postService).sendForReview(updatedPostDTO);
    }

    @Test
    void testUpdatePost_Unauthorized() {
        PostDTO postDTO = mock(PostDTO.class);

        ResponseEntity<PostDTO> response = postController.updatePost("viewer", 1L, postDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(postService, never()).editPost(1L, postDTO);
    }

    @Test
    void testGetPublishedPosts_Success() {
        List<PostDTO> publishedPosts = Arrays.asList(mock(PostDTO.class), mock(PostDTO.class));

        when(postService.getPublishedPosts()).thenReturn(publishedPosts);

        ResponseEntity<List<PostDTO>> response = postController.getPublishedPosts("editor");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(publishedPosts, response.getBody());
        verify(postService).getPublishedPosts();
    }

    @Test
    void testGetPublishedPosts_Unauthorized() {
        ResponseEntity<List<PostDTO>> response = postController.getPublishedPosts(null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(postService, never()).getPublishedPosts();
    }

    @Test
    void testGetDraftPosts_Success() {
        List<PostDTO> draftPosts = Arrays.asList(mock(PostDTO.class), mock(PostDTO.class));

        when(postService.getDraftPosts()).thenReturn(draftPosts);

        ResponseEntity<List<PostDTO>> response = postController.getDraftPosts("editor");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(draftPosts, response.getBody());
        verify(postService).getDraftPosts();
    }

    @Test
    void testGetDraftPosts_Unauthorized() {
        ResponseEntity<List<PostDTO>> response = postController.getDraftPosts(null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(postService, never()).getDraftPosts();
    }

    @Test
    void testGetFilteredPosts_Success() {
        List<PostDTO> filteredPosts = Arrays.asList(mock(PostDTO.class), mock(PostDTO.class));

        when(postService.getPostsFiltered("content", "author", LocalDate.now(), LocalDate.now())).thenReturn(filteredPosts);

        ResponseEntity<List<PostDTO>> response = postController.getFilteredPosts("editor", "content", "author", LocalDate.now(), LocalDate.now());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filteredPosts, response.getBody());
        verify(postService).getPostsFiltered(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testGetFilteredPosts_Unauthorized() {
        ResponseEntity<List<PostDTO>> response = postController.getFilteredPosts("viewer", "content", "author", LocalDate.now(), LocalDate.now());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(postService, never()).getPostsFiltered(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testGetPublishedPostById_Success() {
        PostDTO postDTO = mock(PostDTO.class);

        when(postService.getPublishedPostById(1L)).thenReturn(postDTO);

        ResponseEntity<PostDTO> response = postController.getPublishedPostById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(postDTO, response.getBody());
        verify(postService).getPublishedPostById(1L);
    }

    @Test
    void testGetPostById_Success() {
        PostDTO postDTO = mock(PostDTO.class);

        when(postService.getPostById(1L)).thenReturn(postDTO);

        ResponseEntity<PostDTO> response = postController.getPostById("editor", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(postDTO, response.getBody());
        verify(postService).getPostById(1L);
    }

    @Test
    void testGetPostById_Unauthorized() {
        ResponseEntity<PostDTO> response = postController.getPostById(null, 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(postService, never()).getPostById(1L);
    }
}
