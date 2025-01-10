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
import java.util.Optional;
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
    public PostDTO createOrUpdateDraft(PostDTO postDTO) {
        Post post = postDTOConverter.convertToEntity(postDTO);
        boolean isNewPost = (post.getId() == null);

        post.setStatus(PostStatus.DRAFT);
        post.setLastModifiedDate(LocalDate.now());

        if (isNewPost) {
            post.setCreatedDate(LocalDate.now());
        }

        Post savedPost = postRepository.save(post);

        boolean needsReview = isNewPost;
        if (!isNewPost) {
            Optional<Post> existingPostOpt = postRepository.findById(savedPost.getId());
            if (existingPostOpt.isPresent()) {
                Post existingPost = existingPostOpt.get();
                needsReview = !existingPost.getContent().equals(savedPost.getContent())
                        || !existingPost.getTitle().equals(savedPost.getTitle());
            } else {
                needsReview = true;
            }
        }

        if (needsReview) {
            sendForReview(postDTOConverter.convertToDTO(savedPost));
        }

        return postDTOConverter.convertToDTO(savedPost);
    }

    public void sendForReview(PostDTO postDTO) {
        Post post = postRepository.findById(postDTO.getId())
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + postDTO.getId() + " not found."));

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        postRepository.save(post);

        if (post.getId() != null) {
            reviewClient.deletePendingReviewForPost(post.getId());
        }

        ReviewRequest reviewRequest = new ReviewRequest(post.getId(), post.getAuthor());
        reviewClient.submitPostForReview(reviewRequest);
    }

    @Override
    public PostDTO getPublishedPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        if (!PostStatus.PUBLISHED.toString().equalsIgnoreCase(post.getStatus().toString())) {
            throw new PostNotFoundException("Post with ID " + id + " is not published.");
        }

        return postDTOConverter.convertToDTO(post);
    }

    @Override
    public PostDTO editPost(Long id, PostDTO postDTO) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        String oldTitle = existingPost.getTitle();
        String oldContent = existingPost.getContent();

        if (postDTO.getTitle() != null && !postDTO.getTitle().isBlank()) {
            existingPost.setTitle(postDTO.getTitle());
        }
        if (postDTO.getContent() != null && !postDTO.getContent().isBlank()) {
            existingPost.setContent(postDTO.getContent());
        }
        existingPost.setRemarks(postDTO.getRemarks());
        existingPost.setLastModifiedDate(LocalDate.now());

        Post updatedPost = postRepository.save(existingPost);

        if (updatedPost.getStatus() == PostStatus.DRAFT) {
            boolean changed =
                    !oldTitle.equals(updatedPost.getTitle())
                            || !oldContent.equals(updatedPost.getContent());
            if (changed) {
                sendForReview(postDTOConverter.convertToDTO(updatedPost));
            }
        }

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

    @Override
    public List<PostDTO> getDraftPosts() {
        List<Post> draftPosts = postRepository.findByStatus(PostStatus.DRAFT);
        return draftPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsFiltered(String content, String author, LocalDate createdDate, LocalDate lastModifiedDate) {
        List<Post> filteredPosts = postRepository.findPostsByFilters(
                content,
                author,
                createdDate,
                lastModifiedDate
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
