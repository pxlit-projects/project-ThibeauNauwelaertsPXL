package org.JavaPE.controller;

import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestHeader(value = "X-User-Role", required = false) String role, @RequestBody PostDTO postDTO) {
        if (!"EDITOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If status is PUBLISHED, create the post as published
        if (PostStatus.PUBLISHED.toString().equals(postDTO.getStatus())) {
            PostDTO responseDTO = postService.createPost(postDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } else {
            // If status is DRAFT, save the post as draft
            PostDTO responseDTO = postService.saveDraft(postDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @RequestBody PostDTO postDTO
    ) {
        // Authorization check
        if (!"EDITOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PostDTO responseDTO = postService.editPost(id, postDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/published")
    public ResponseEntity<List<PostDTO>> getPublishedPosts(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PostDTO> publishedPosts = postService.getPublishedPosts();
        return ResponseEntity.ok(publishedPosts);
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<PostDTO>> getFilteredPosts(@RequestHeader(value = "X-User-Role", required = false) String role, @RequestBody PostDTO postDTO) {
        if (!"EDITOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PostDTO> filteredPosts = postService.getPostsFiltered(postDTO);
        return ResponseEntity.ok(filteredPosts);
    }

}
