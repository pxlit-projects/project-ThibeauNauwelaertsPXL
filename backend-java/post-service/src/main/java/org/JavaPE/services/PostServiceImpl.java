package org.JavaPE.services;

import jakarta.transaction.Transactional;
import org.JavaPE.client.ReviewClient;
import org.JavaPE.controller.Request.ReviewRequest;
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

    private final PostRepository postRepository;
    private final PostDTOConverter postDTOConverter;
    private final ReviewClient reviewClient;

    public PostServiceImpl(PostRepository postRepository, PostDTOConverter postDTOConverter, ReviewClient reviewClient) {
        this.postRepository = postRepository;
        this.postDTOConverter = postDTOConverter;
        this.reviewClient = reviewClient;
    }

    @Override
    public PostDTO createPost(PostDTO postDTO) {
        Post post = postDTOConverter.convertToEntity(postDTO);
        post.setStatus(PostStatus.DRAFT);

        Post savedPost = postRepository.save(post);

        return postDTOConverter.convertToDTO(savedPost);
    }

    @Override
    public PostDTO saveDraft(PostDTO postDTO) {
        Post post = postDTOConverter.convertToEntity(postDTO);

        boolean isNewPost = post.getId() == null;

        post.setStatus(PostStatus.DRAFT);
        post.setLastModifiedDate(LocalDate.now());

        if (isNewPost) {
            post.setCreatedDate(LocalDate.now());
        }

        Post savedPost = postRepository.save(post);

        if (isNewPost || hasBeenEdited(savedPost)) {
            sendForReview(postDTOConverter.convertToDTO(savedPost));
        }

        return postDTOConverter.convertToDTO(savedPost);
    }

    private boolean hasBeenEdited(Post post) {
        Post existingPost = postRepository.findById(post.getId()).orElse(null);
        if (existingPost == null) {
            return true;
        }
        return !existingPost.getContent().equals(post.getContent()) ||
                !existingPost.getTitle().equals(post.getTitle());
    }

    public void sendForReview(PostDTO post) {
        ReviewRequest reviewRequest = new ReviewRequest(post.getId(), post.getAuthor());
        reviewClient.submitPostForReview(reviewRequest);
    }

    @Override
    public PostDTO getPublishedPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        if (!post.getStatus().toString().equalsIgnoreCase("PUBLISHED")) {
            throw new PostNotFoundException("Post with ID " + id + " is not published.");
        }

        return postDTOConverter.convertToDTO(post);
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
        existingPost.setRemarks(postDTO.getRemarks());

        Post updatedPost = postRepository.save(existingPost);

        return postDTOConverter.convertToDTO(updatedPost);
    }

    @Override
    public void publishPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + postId + " not found."));

        post.setStatus(PostStatus.PUBLISHED);
        post.setLastModifiedDate(LocalDate.now());
        postRepository.save(post);
    }

    @Override
    public List<PostDTO> getPublishedPosts() {
        List<Post> publishedPosts = postRepository.findByStatus(PostStatus.PUBLISHED);

        return publishedPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> getDraftPosts() {
        List<Post> draftPosts = postRepository.findByStatus(PostStatus.DRAFT);

        return draftPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsFiltered(String content, String author, LocalDate createdDate, LocalDate lastModifiedDate) {
        PostDTO filterDTO = new PostDTO();
        filterDTO.setContent(content);
        filterDTO.setAuthor(author);
        filterDTO.setCreatedDate(createdDate);
        filterDTO.setLastModifiedDate(lastModifiedDate);

        List<Post> filteredPosts = postRepository.findPostsByFilters(
                filterDTO.getContent(),
                filterDTO.getAuthor(),
                filterDTO.getCreatedDate(),
                filterDTO.getLastModifiedDate()
        );

        return filteredPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        return postDTOConverter.convertToDTO(post);
    }

}
