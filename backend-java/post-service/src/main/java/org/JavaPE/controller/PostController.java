package org.JavaPE.controller;

import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.services.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody PostDTO postDTO) {
        logger.info("Received request to create a post with role: {}", role);

        if (!role.equals("editor")) {
            logger.warn("Unauthorized attempt to create a post. Role: {}", role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PostDTO responseDTO = postService.createOrUpdateDraft(postDTO);
        logger.info("Post created successfully with ID: {}", responseDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @RequestBody PostDTO postDTO) {
        if (!"editor".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Just update fields (no “new vs updated” logic):
        PostDTO updatedPost = postService.editPost(id, postDTO);

        return ResponseEntity.ok(updatedPost);
    }


    @GetMapping("/published")
    public ResponseEntity<List<PostDTO>> getPublishedPosts(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        logger.info("Received request to fetch published posts with role: {}", role);

        if (role == null || role.isBlank()) {
            logger.warn("Unauthorized attempt to fetch published posts. Role is missing.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PostDTO> publishedPosts = postService.getPublishedPosts();
        logger.info("Successfully fetched {} published posts", publishedPosts.size());
        return ResponseEntity.ok(publishedPosts);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<PostDTO>> getDraftPosts(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        logger.info("Received request to fetch draft posts with role: {}", role);

        if (role == null || role.isBlank()) {
            logger.warn("Unauthorized attempt to fetch draft posts. Role is missing.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PostDTO> draftPosts = postService.getDraftPosts();
        logger.info("Successfully fetched {} draft posts", draftPosts.size());
        return ResponseEntity.ok(draftPosts);
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<PostDTO>> getFilteredPosts(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastModifiedDate) {
        logger.info("Received request to fetch filtered posts with role: {}", role);

        if (!"editor".equals(role)) {
            logger.warn("Unauthorized attempt to fetch filtered posts. Role: {}", role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.debug("Filter parameters - Title: {}, Author: {}, Created Date: {}, Last Modified Date: {}",
                title, author, createdDate, lastModifiedDate);

        List<PostDTO> filteredPosts = postService.getPostsFiltered(title, author, createdDate, lastModifiedDate);
        logger.info("Successfully fetched {} filtered posts", filteredPosts.size());
        return ResponseEntity.ok(filteredPosts);
    }

    @GetMapping("/published/{id}")
    public ResponseEntity<PostDTO> getPublishedPostById(@PathVariable Long id) {
        logger.info("Received request to fetch published post with ID: {}", id);

        PostDTO postDTO = postService.getPublishedPostById(id);
        if (postDTO == null) {
            logger.warn("Published post with ID: {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        logger.info("Successfully fetched published post with ID: {}", id);
        return ResponseEntity.ok(postDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        logger.info("Received request to fetch post with ID: {} and role: {}", id, role);

        if (role == null || role.isBlank()) {
            logger.warn("Unauthorized attempt to fetch post with ID: {}. Role is missing.", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        PostDTO post = postService.getPostById(id);
        logger.info("Successfully fetched post with ID: {}", id);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishPost(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        logger.info("Received request to publish post with ID: {} by role: {}", id, role);

        if (!"editor".equals(role)) {
            logger.warn("Unauthorized attempt to publish post with ID: {}. Role: {}", id, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            postService.publishPost(id);
            logger.info("Post with ID: {} published successfully.", id);
            return ResponseEntity.ok().build();
        } catch (org.JavaPE.exception.PostNotFoundException e) {
            logger.error("Post with ID: {} not found. Cannot publish.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error occurred while publishing post with ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}