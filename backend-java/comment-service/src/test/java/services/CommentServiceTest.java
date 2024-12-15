package services;

import org.JavaPE.client.PostClient;
import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Comment;
import org.JavaPE.exception.CommentNotFoundException;
import org.JavaPE.exception.InvalidAuthorException;
import org.JavaPE.repository.CommentRepository;
import org.JavaPE.controller.converter.CommentConverter;
import org.JavaPE.services.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostClient postClient;

    @Mock
    private CommentConverter commentConverter;

    @Mock
    private Comment comment;

    @Mock
    private CommentDTO commentDTO;

    @Mock
    private PostDTO postDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddCommentToPost_Success() {
        // Arrange
        Long postId = 1L;
        when(postClient.getPublishedPostById(postId, "editor")).thenReturn(postDTO);
        when(commentConverter.toEntity(commentDTO)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentConverter.toDTO(comment)).thenReturn(commentDTO);

        // Act
        CommentDTO result = commentService.addCommentToPost(postId, commentDTO);

        // Assert
        assertNotNull(result);
        verify(postClient).getPublishedPostById(postId, "editor");
        verify(commentRepository).save(comment);
        verify(commentConverter).toEntity(commentDTO);
        verify(commentConverter).toDTO(comment);
    }

    @Test
    void testAddCommentToPost_PostNotPublished() {
        // Arrange
        Long postId = 1L;
        when(postClient.getPublishedPostById(postId, "editor")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.addCommentToPost(postId, commentDTO)
        );

        assertEquals("Cannot add comment. Post with ID 1 is not published.", exception.getMessage());
        verify(postClient).getPublishedPostById(postId, "editor");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testGetCommentsByPostId() {
        // Arrange
        Long postId = 1L;
        when(commentRepository.findByPostId(postId)).thenReturn(List.of(comment));
        when(commentConverter.toDTO(comment)).thenReturn(commentDTO);

        // Act
        List<CommentDTO> result = commentService.getCommentsByPostId(postId);

        // Assert
        assertEquals(1, result.size());
        verify(commentRepository).findByPostId(postId);
        verify(commentConverter).toDTO(comment);
    }

    @Test
    void testUpdateComment_Success() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn("Author");
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentConverter.toDTO(comment)).thenReturn(commentDTO);
        when(commentDTO.getAuthor()).thenReturn("Author");

        // Act
        CommentDTO result = commentService.updateComment(commentId, commentDTO);

        // Assert
        assertNotNull(result);
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(comment);
        verify(commentConverter).toDTO(comment);
    }

    @Test
    void testUpdateComment_CommentNotFound() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CommentNotFoundException.class,
                () -> commentService.updateComment(commentId, commentDTO)
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testUpdateComment_InvalidAuthor() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn("OriginalAuthor");
        when(commentDTO.getAuthor()).thenReturn("AnotherAuthor");

        // Act & Assert
        assertThrows(InvalidAuthorException.class,
                () -> commentService.updateComment(commentId, commentDTO)
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testDeleteComment_Success() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn("Author");

        // Act
        commentService.deleteComment(commentId, "Author");

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CommentNotFoundException.class,
                () -> commentService.deleteComment(commentId, "Author")
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void testDeleteComment_InvalidAuthor() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn("OriginalAuthor");

        // Act & Assert
        assertThrows(InvalidAuthorException.class,
                () -> commentService.deleteComment(commentId, "AnotherAuthor")
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void testEditComment_Success() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn("Author");
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentConverter.toDTO(comment)).thenReturn(commentDTO);

        // Act
        CommentDTO result = commentService.editComment(commentId, "Author", commentDTO);

        // Assert
        assertNotNull(result);
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(comment);
        verify(commentConverter).toDTO(comment);
    }

    @Test
    void testEditComment_CommentNotFound() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CommentNotFoundException.class,
                () -> commentService.editComment(commentId, "Author", commentDTO)
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testEditComment_InvalidAuthor() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn("OriginalAuthor");

        // Act & Assert
        assertThrows(InvalidAuthorException.class,
                () -> commentService.editComment(commentId, "AnotherAuthor", commentDTO)
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }
}
