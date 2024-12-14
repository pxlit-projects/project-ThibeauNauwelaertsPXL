package org.JavaPE.controller;

import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.services.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDTO> addCommentToPost(
            @PathVariable Long postId,
            @RequestBody CommentDTO commentDTO) {
        logger.info("Received request to add a comment to post with ID: {}", postId);
        commentDTO.setPostId(postId);
        CommentDTO savedComment = commentService.addCommentToPost(postId, commentDTO);
        logger.info("Successfully added comment to post with ID: {}", postId);
        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        logger.info("Received request to fetch comments for post with ID: {}", postId);
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        logger.info("Successfully fetched {} comments for post with ID: {}", comments.size(), postId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDTO updatedCommentDTO) {
        logger.info("Received request to update comment with ID: {}", commentId);
        CommentDTO updatedComment = commentService.updateComment(commentId, updatedCommentDTO);
        logger.info("Successfully updated comment with ID: {}", commentId);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Role") String currentUser) {
        logger.info("Received request to delete comment with ID: {} by user: {}", commentId, currentUser);
        commentService.deleteComment(commentId, currentUser);
        logger.info("Successfully deleted comment with ID: {}", commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{commentId}/edit")
    public ResponseEntity<CommentDTO> editComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Role") String currentUser,
            @RequestBody CommentDTO commentDTO) {
        logger.info("Received request to edit comment with ID: {} by user: {}", commentId, currentUser);
        CommentDTO updatedComment = commentService.editComment(commentId, currentUser, commentDTO);
        logger.info("Successfully edited comment with ID: {}", commentId);
        return ResponseEntity.ok(updatedComment);
    }
}
