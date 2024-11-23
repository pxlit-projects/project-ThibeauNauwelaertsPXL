package org.JavaPE.services;

import jakarta.transaction.Transactional;
import org.JavaPE.controller.converter.PostDTOConverter;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.exception.PostNotFoundException;
import org.JavaPE.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private PostRepository postRepository;
    private PostDTOConverter postDTOConverter;

    public PostServiceImpl(PostRepository postRepository, PostDTOConverter postDTOConverter) {
        this.postRepository = postRepository;
        this.postDTOConverter = postDTOConverter;
    }

    @Override
    public PostDTO createPost(PostDTO postDTO) {
        Post post = postDTOConverter.convertToEntity(postDTO);

        if (post.getStatus() == null) {
            post.setStatus(PostStatus.PUBLISHED);
        }

        Post savedPost = postRepository.save(post);

        return postDTOConverter.convertToDTO(savedPost);
    }

    @Override
    public PostDTO saveDraft(PostDTO postDTO) {
        Post post = postDTOConverter.convertToEntity(postDTO);

        post.setStatus(PostStatus.DRAFT);
        post.setCreatedDate(LocalDate.now());
        post.setLastModifiedDate(LocalDate.now());

        Post savedPost = postRepository.save(post);

        return postDTOConverter.convertToDTO(savedPost);
    }

    @Override
    public PostDTO editPost(Long id, PostDTO postDTO) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        if (postDTO.getTitle() != null && !postDTO.getTitle().isBlank()) {
            existingPost.setTitle(postDTO.getTitle());
        }
        if (postDTO.getContent() != null && !postDTO.getContent().isBlank()) {
            existingPost.setContent(postDTO.getContent());
        }
        existingPost.setLastModifiedDate(LocalDate.now());

        Post updatedPost = postRepository.save(existingPost);

        return postDTOConverter.convertToDTO(updatedPost);
    }

    @Override
    public List<PostDTO> getPublishedPosts() {
        // Fetch posts with status PUBLISHED from the database
        List<Post> publishedPosts = postRepository.findByStatus(PostStatus.PUBLISHED);

        // Convert the list of entities to a list of DTOs
        return publishedPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsFiltered(PostDTO filterDTO) {
        String content = filterDTO.getContent();
        String author = filterDTO.getAuthor();
        LocalDate startDate = filterDTO.getCreatedDate();
        LocalDate endDate = filterDTO.getLastModifiedDate();

        List<Post> filteredPosts = postRepository.findPostsByFilters(
                content, author, startDate, endDate
        );

        return filteredPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }
}
