package org.JavaPE.services;

import lombok.RequiredArgsConstructor;
import org.JavaPE.controller.converter.PostDTOConverter;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostDTOConverter postDTOConverter;

    public PostDTO createPost(Post post) {
        post.setStatus(PostStatus.DRAFT); // Default to draft
        post.setCreatedDate(LocalDate.now());
        Post savedPost = postRepository.save(post);
        return postDTOConverter.convertToDTO(savedPost);
    }

    public PostDTO updatePost(Long id, Post updatedPost) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setLastModifiedDate(LocalDate.now());
        Post savedPost = postRepository.save(existingPost);
        return postDTOConverter.convertToDTO(savedPost);
    }

    public List<PostDTO> getPublishedPosts() {
        List<Post> posts = postRepository.findByStatus(PostStatus.PUBLISHED);
        return posts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> filterPosts(String title, String author) {
        // Filtering logic can be added here later
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }
}
