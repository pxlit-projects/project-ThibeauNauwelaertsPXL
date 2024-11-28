package org.JavaPE.controller.converter;

import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PostDTOConverter {
    public PostDTO convertToDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setAuthor(post.getAuthor());
        postDTO.setCreatedDate(post.getCreatedDate());
        postDTO.setLastModifiedDate(post.getLastModifiedDate());
        postDTO.setStatus(post.getStatus().name()); // Convert Enum to String
        postDTO.setRemarks(post.getRemarks()); // Set the remarks
        return postDTO;
    }

    public Post convertToEntity(PostDTO postDTO) {
        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setAuthor(postDTO.getAuthor());
        post.setStatus(PostStatus.PUBLISHED); // Automatically set status to PUBLISHED
        post.setCreatedDate(LocalDate.now()); // Set the creation date
        post.setLastModifiedDate(LocalDate.now()); // Set the last modified date
        post.setRemarks(postDTO.getRemarks()); // Set the remarks
        return post;
    }
}
