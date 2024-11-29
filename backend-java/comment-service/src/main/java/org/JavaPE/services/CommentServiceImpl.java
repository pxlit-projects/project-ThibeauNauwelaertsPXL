package org.JavaPE.services;

import org.JavaPE.client.PostClient;
import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Comment;
import org.JavaPE.exception.CommentNotFoundException;
import org.JavaPE.repository.CommentRepository;
import org.JavaPE.controller.converter.CommentConverter;
import org.springframework.stereotype.Service;

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
        // Validate if the post is published
        PostDTO postDTO = postClient.getPublishedPostById(postId, "EDITOR");

        if (postDTO == null) {
            throw new IllegalArgumentException("Cannot add comment. Post with ID " + postId + " is not published.");
        }

        // Create a new Comment entity and save it
        Comment comment = commentConverter.toEntity(commentDTO);

        comment = commentRepository.save(comment);

        return new CommentDTO(comment.getPostId(), comment.getAuthor(), comment.getContent());
    }


    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(commentConverter::toDTO).collect(Collectors.toList());
    }

    @Override
    public CommentDTO updateComment(Long commentId, CommentDTO updatedCommentDTO) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        // Check if the author matches
        if (!comment.getAuthor().equals(updatedCommentDTO.getAuthor())) {
            throw new Exception("You are not authorized to update this comment");
        }

        // Update the content
        comment.setContent(updatedCommentDTO.getContent());

        Comment updatedComment = commentRepository.save(comment);
        return commentConverter.toDTO(updatedComment);
    }


    @Override
    public void deleteComment(Long commentId, String currentUser) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        // Check if the author matches
        if (!comment.getAuthor().equals(currentUser)) {
            throw new Exception("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

}
