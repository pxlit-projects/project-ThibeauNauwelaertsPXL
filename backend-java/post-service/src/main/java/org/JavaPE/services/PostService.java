package org.JavaPE.services;

import org.JavaPE.controller.dto.PostDTO;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    PostDTO saveDraft(PostDTO postDTO);
    PostDTO editPost(Long id, PostDTO postDTO);
    List<PostDTO> getPublishedPosts();
    List<PostDTO> getDraftPosts();
    List<PostDTO> getPostsFiltered(PostDTO postDTO);
    PostDTO getPostById(Long id);

    void sendForReview(PostDTO updatedPost);
}
