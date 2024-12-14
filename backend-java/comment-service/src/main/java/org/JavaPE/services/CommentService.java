package org.JavaPE.services;

import org.JavaPE.controller.DTO.CommentDTO;

import java.util.List;

public interface CommentService {
    CommentDTO addCommentToPost(Long postId, CommentDTO commentDTO);
    List<CommentDTO> getCommentsByPostId(Long postId);
    CommentDTO updateComment(Long commentId, CommentDTO updatedCommentDTO);
    void deleteComment(Long commentId, String currentUser);
    CommentDTO editComment(Long commentId, String currentUser, CommentDTO commentDTO);
}
