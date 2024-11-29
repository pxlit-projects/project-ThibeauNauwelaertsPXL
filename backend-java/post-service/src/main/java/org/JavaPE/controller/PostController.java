package org.JavaPE.controller;

import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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

        PostDTO responseDTO = postService.saveDraft(postDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @RequestBody PostDTO postDTO
    ) {
        if (!"EDITOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PostDTO updatedPost = postService.editPost(id, postDTO);

        // If the updated post is still a draft, send it for review
        if (PostStatus.DRAFT.toString().equals(updatedPost.getStatus())) {
            postService.sendForReview(updatedPost);
        }

        return ResponseEntity.ok(updatedPost);
    }


    @GetMapping("/published")
    public ResponseEntity<List<PostDTO>> getPublishedPosts(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PostDTO> publishedPosts = postService.getPublishedPosts();
        return ResponseEntity.ok(publishedPosts);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<PostDTO>> getDraftPosts(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PostDTO> draftPosts = postService.getDraftPosts();
        return ResponseEntity.ok(draftPosts);
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<PostDTO>> getFilteredPosts(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastModifiedDate) {

        // Authorization check
        if (!"EDITOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Create a filter DTO from query parameters
        PostDTO filterDTO = new PostDTO();
        filterDTO.setContent(content);
        filterDTO.setAuthor(author);
        filterDTO.setCreatedDate(createdDate);
        filterDTO.setLastModifiedDate(lastModifiedDate);

        // Fetch filtered posts
        List<PostDTO> filteredPosts = postService.getPostsFiltered(filterDTO);
        return ResponseEntity.ok(filteredPosts);
    }

    @GetMapping("/published/{id}")
    public ResponseEntity<PostDTO> getPublishedPostById(@PathVariable Long id) {
        PostDTO postDTO = postService.getPublishedPostById(id);
        return ResponseEntity.ok(postDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@RequestHeader(value = "X-User-Role", required = false) String role, @PathVariable Long id) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

}
