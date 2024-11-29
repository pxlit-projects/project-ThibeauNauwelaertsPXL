package org.JavaPE.services;

import org.JavaPE.controller.DTO.CommentDTO;

import java.util.List;

public interface CommentService {
    CommentDTO addCommentToPost(Long postId, CommentDTO commentDTO); // Add comment with DTO
    List<CommentDTO> getCommentsByPostId(Long postId); // Get comments for a post
    CommentDTO updateComment(Long commentId, CommentDTO updatedCommentDTO) throws Exception;
    void deleteComment(Long commentId, String currentUser) throws Exception;
}
