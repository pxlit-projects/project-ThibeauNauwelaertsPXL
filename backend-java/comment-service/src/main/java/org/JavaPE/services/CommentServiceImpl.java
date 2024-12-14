package org.JavaPE.services;

import org.JavaPE.client.PostClient;
import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Comment;
import org.JavaPE.exception.CommentNotFoundException;
import org.JavaPE.exception.InvalidAuthorException;
import org.JavaPE.repository.CommentRepository;
import org.JavaPE.controller.converter.CommentConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostClient postClient;
    private final CommentConverter commentConverter;

    public CommentServiceImpl(CommentRepository commentRepository, PostClient postClient, CommentConverter commentConverter) {
        this.commentRepository = commentRepository;
        this.postClient = postClient;
        this.commentConverter = commentConverter;
    }

    @Override
    public CommentDTO addCommentToPost(Long postId, CommentDTO commentDTO) {
        PostDTO postDTO = postClient.getPublishedPostById(postId, "editor");

        if (postDTO == null) {
            throw new IllegalArgumentException("Cannot add comment. Post with ID " + postId + " is not published.");
        }

        Comment comment = commentConverter.toEntity(commentDTO);

        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }

        comment = commentRepository.save(comment);

        return commentConverter.toDTO(comment);
    }

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(commentConverter::toDTO).collect(Collectors.toList());
    }

    @Override
    public CommentDTO updateComment(Long commentId, CommentDTO updatedCommentDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getAuthor().equals(updatedCommentDTO.getAuthor())) {
            throw new InvalidAuthorException("You are not authorized to update this comment");
        }

        // Update the content
        comment.setContent(updatedCommentDTO.getContent());

        Comment updatedComment = commentRepository.save(comment);
        return commentConverter.toDTO(updatedComment);
    }

    @Override
    public void deleteComment(Long commentId, String currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getAuthor().equals(currentUser)) {
            throw new InvalidAuthorException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    public CommentDTO editComment(Long commentId, String currentUser, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getAuthor().equals(currentUser)) {
            throw new InvalidAuthorException("You are not authorized to edit this comment");
        }

        comment.setContent(commentDTO.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return commentConverter.toDTO(updatedComment);
    }
}
