package org.JavaPE.services;

import org.JavaPE.controller.dto.PostDTO;

import java.time.LocalDate;
import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    PostDTO saveDraft(PostDTO postDTO);
    PostDTO editPost(Long id, PostDTO postDTO);
    List<PostDTO> getPublishedPosts();
    List<PostDTO> getDraftPosts();
    List<PostDTO> getPostsFiltered(String content, String author, LocalDate createdDate, LocalDate lastModifiedDate);
    PostDTO getPostById(Long id);
    void publishPost(Long id);
    void sendForReview(PostDTO updatedPost);
    PostDTO getPublishedPostById(Long id);
}
