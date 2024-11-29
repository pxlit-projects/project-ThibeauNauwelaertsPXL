package org.JavaPE.controller;

import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.services.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDTO> addCommentToPost(
            @PathVariable Long postId,
            @RequestBody CommentDTO commentDTO) {
        commentDTO.setPostId(postId);
        CommentDTO savedComment = commentService.addCommentToPost(postId, commentDTO);
        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDTO updatedCommentDTO) throws Exception {
        CommentDTO updatedComment = commentService.updateComment(commentId, updatedCommentDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Role") String currentUser) throws Exception {
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }

}
