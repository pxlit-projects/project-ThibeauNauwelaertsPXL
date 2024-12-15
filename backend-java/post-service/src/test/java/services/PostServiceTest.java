package services;

import org.JavaPE.client.ReviewClient;
import org.JavaPE.controller.Request.ReviewRequest;
import org.JavaPE.controller.converter.PostDTOConverter;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.exception.PostNotFoundException;
import org.JavaPE.repository.PostRepository;
import org.JavaPE.services.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostDTOConverter postDTOConverter;

    @Mock
    private ReviewClient reviewClient;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePost() {
        PostDTO postDTO = mock(PostDTO.class);
        Post post = mock(Post.class);
        Post savedPost = mock(Post.class);
        PostDTO savedPostDTO = mock(PostDTO.class);

        when(postDTOConverter.convertToEntity(postDTO)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(savedPost);
        when(postDTOConverter.convertToDTO(savedPost)).thenReturn(savedPostDTO);

        PostDTO result = postService.createPost(postDTO);

        assertNotNull(result);
        verify(postDTOConverter).convertToEntity(postDTO);
        verify(postRepository).save(post);
        verify(postDTOConverter).convertToDTO(savedPost);
    }

    @Test
    void testSaveDraft() {
        PostDTO postDTO = mock(PostDTO.class);
        Post post = mock(Post.class);
        Post savedPost = mock(Post.class);
        PostDTO savedPostDTO = mock(PostDTO.class);

        when(postDTOConverter.convertToEntity(postDTO)).thenReturn(post);
        when(post.getId()).thenReturn(null); // Simulate a new post
        when(postRepository.save(post)).thenReturn(savedPost);
        when(postDTOConverter.convertToDTO(savedPost)).thenReturn(savedPostDTO);

        PostDTO result = postService.saveDraft(postDTO);

        assertNotNull(result);
        verify(postDTOConverter).convertToEntity(postDTO);
        verify(postRepository).save(post);
        verify(postDTOConverter, times(2)).convertToDTO(savedPost);
    }


    @Test
    void testGetPublishedPostById_Success() {
        Post post = mock(Post.class);
        PostDTO postDTO = mock(PostDTO.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(post.getStatus()).thenReturn(PostStatus.PUBLISHED);
        when(postDTOConverter.convertToDTO(post)).thenReturn(postDTO);

        PostDTO result = postService.getPublishedPostById(1L);

        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(postDTOConverter).convertToDTO(post);
    }

    @Test
    void testGetPublishedPostById_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPublishedPostById(1L));
        verify(postRepository).findById(1L);
    }

    @Test
    void testEditPost() {
        PostDTO postDTO = mock(PostDTO.class);
        Post existingPost = mock(Post.class);
        Post updatedPost = mock(Post.class);
        PostDTO updatedPostDTO = mock(PostDTO.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(existingPost)).thenReturn(updatedPost);
        when(postDTOConverter.convertToDTO(updatedPost)).thenReturn(updatedPostDTO);

        PostDTO result = postService.editPost(1L, postDTO);

        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(postRepository).save(existingPost);
        verify(postDTOConverter).convertToDTO(updatedPost);
    }

    @Test
    void testPublishPost() {
        Post post = mock(Post.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.publishPost(1L);

        verify(postRepository).findById(1L);
        verify(post).setStatus(PostStatus.PUBLISHED);
        verify(post).setLastModifiedDate(any(LocalDate.class));
        verify(postRepository).save(post);
    }

    @Test
    void testGetPublishedPosts() {
        List<Post> posts = Arrays.asList(mock(Post.class), mock(Post.class));
        PostDTO postDTO1 = mock(PostDTO.class);
        PostDTO postDTO2 = mock(PostDTO.class);

        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(posts);
        when(postDTOConverter.convertToDTO(posts.get(0))).thenReturn(postDTO1);
        when(postDTOConverter.convertToDTO(posts.get(1))).thenReturn(postDTO2);

        List<PostDTO> result = postService.getPublishedPosts();

        assertEquals(2, result.size());
        verify(postRepository).findByStatus(PostStatus.PUBLISHED);
        verify(postDTOConverter, times(2)).convertToDTO(any(Post.class));
    }

    @Test
    void testGetDraftPosts() {
        List<Post> posts = Arrays.asList(mock(Post.class), mock(Post.class));
        PostDTO postDTO1 = mock(PostDTO.class);
        PostDTO postDTO2 = mock(PostDTO.class);

        when(postRepository.findByStatus(PostStatus.DRAFT)).thenReturn(posts);
        when(postDTOConverter.convertToDTO(posts.get(0))).thenReturn(postDTO1);
        when(postDTOConverter.convertToDTO(posts.get(1))).thenReturn(postDTO2);

        List<PostDTO> result = postService.getDraftPosts();

        assertEquals(2, result.size());
        verify(postRepository).findByStatus(PostStatus.DRAFT);
        verify(postDTOConverter, times(2)).convertToDTO(any(Post.class));
    }

    @Test
    void testGetPostsFiltered() {
        List<Post> posts = Arrays.asList(mock(Post.class), mock(Post.class));
        PostDTO postDTO1 = mock(PostDTO.class);
        PostDTO postDTO2 = mock(PostDTO.class);

        when(postRepository.findPostsByFilters(any(), any(), any(), any())).thenReturn(posts);
        when(postDTOConverter.convertToDTO(posts.get(0))).thenReturn(postDTO1);
        when(postDTOConverter.convertToDTO(posts.get(1))).thenReturn(postDTO2);

        List<PostDTO> result = postService.getPostsFiltered("content", "author", LocalDate.now(), LocalDate.now());

        assertEquals(2, result.size());
        verify(postRepository).findPostsByFilters(any(), any(), any(), any());
        verify(postDTOConverter, times(2)).convertToDTO(any(Post.class));
    }

    @Test
    void testGetPostById_Success() {
        Post post = mock(Post.class);
        PostDTO postDTO = mock(PostDTO.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postDTOConverter.convertToDTO(post)).thenReturn(postDTO);

        PostDTO result = postService.getPostById(1L);

        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(postDTOConverter).convertToDTO(post);
    }

    @Test
    void testGetPostById_PostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPostById(1L));
        verify(postRepository).findById(1L);
    }

    @Test
    void testSendForReview() {
        PostDTO postDTO = mock(PostDTO.class);
        when(postDTO.getId()).thenReturn(1L);
        when(postDTO.getAuthor()).thenReturn("author");

        postService.sendForReview(postDTO);

        verify(reviewClient).submitPostForReview(any(ReviewRequest.class));
    }
}
