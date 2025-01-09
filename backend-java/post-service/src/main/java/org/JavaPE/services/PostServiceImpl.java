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
        System.out.println("=== createOrUpdateDraft() called ===");
        System.out.println("Incoming postDTO: ID=" + postDTO.getId()
                + ", Title=" + postDTO.getTitle()
                + ", Content=" + postDTO.getContent());

        Post post = postDTOConverter.convertToEntity(postDTO);
        boolean isNewPost = (post.getId() == null);

        post.setStatus(PostStatus.DRAFT);
        post.setLastModifiedDate(LocalDate.now());

        if (isNewPost) {
            post.setCreatedDate(LocalDate.now());
            System.out.println("createOrUpdateDraft: This is a NEW post.");
        } else {
            System.out.println("createOrUpdateDraft: This is an UPDATE for post ID=" + post.getId());
        }

        Post savedPost = postRepository.save(post);
        System.out.println("Post saved. ID=" + savedPost.getId()
                + ", Title=" + savedPost.getTitle()
                + ", Content=" + savedPost.getContent());

        boolean needsReview = isNewPost;

        if (!isNewPost) {
            Optional<Post> existingPostOpt = postRepository.findById(savedPost.getId());
            if (existingPostOpt.isPresent()) {
                Post existingPost = existingPostOpt.get();
                needsReview = !existingPost.getContent().equals(savedPost.getContent())
                        || !existingPost.getTitle().equals(savedPost.getTitle());
                System.out.println("Comparing old vs new content/titles for review. NeedsReview=" + needsReview);
            } else {
                needsReview = true;
            }
        }

        if (needsReview) {
            System.out.println("createOrUpdateDraft: Sending for review...");
            sendForReview(postDTOConverter.convertToDTO(savedPost));
        }

        return postDTOConverter.convertToDTO(savedPost);
    }

    public void sendForReview(PostDTO postDTO) {
        System.out.println("=== sendForReview() called ===");
        System.out.println("PostDTO: ID=" + postDTO.getId()
                + ", Title=" + postDTO.getTitle()
                + ", Content=" + postDTO.getContent());

        Post post = postRepository.findById(postDTO.getId())
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + postDTO.getId() + " not found."));

        // Overwrite with newest content from postDTO
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        Post updated = postRepository.save(post);

        System.out.println("Post updated in DB before review. ID=" + updated.getId()
                + ", Title=" + updated.getTitle()
                + ", Content=" + updated.getContent());

        if (post.getId() != null) {
            System.out.println("Deleting old pending review (if any) for postId=" + post.getId());
            reviewClient.deletePendingReviewForPost(post.getId());
        }

        ReviewRequest reviewRequest = new ReviewRequest(post.getId(), post.getAuthor());
        System.out.println("Submitting new review for postId=" + post.getId()
                + ", Author=" + post.getAuthor());
        reviewClient.submitPostForReview(reviewRequest);
    }

    @Override
    public PostDTO getPublishedPostById(Long id) {
        System.out.println("getPublishedPostById called. ID=" + id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        if (!post.getStatus().toString().equalsIgnoreCase("PUBLISHED")) {
            throw new PostNotFoundException("Post with ID " + id + " is not published.");
        }
        return postDTOConverter.convertToDTO(post);
    }

    @Override
    public PostDTO editPost(Long id, PostDTO postDTO) {
        System.out.println("=== editPost() called ===");
        System.out.println("Incoming postDTO: ID=" + id
                + ", NewTitle=" + postDTO.getTitle()
                + ", NewContent=" + postDTO.getContent());

        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        String oldTitle = existingPost.getTitle();
        String oldContent = existingPost.getContent();

        System.out.println("Old data => Title=" + oldTitle + ", Content=" + oldContent);

        if (postDTO.getTitle() != null && !postDTO.getTitle().isBlank()) {
            existingPost.setTitle(postDTO.getTitle());
        }
        if (postDTO.getContent() != null && !postDTO.getContent().isBlank()) {
            existingPost.setContent(postDTO.getContent());
        }
        existingPost.setRemarks(postDTO.getRemarks());
        existingPost.setLastModifiedDate(LocalDate.now());

        Post updatedPost = postRepository.save(existingPost);

        System.out.println("Post updated => ID=" + updatedPost.getId()
                + ", Title=" + updatedPost.getTitle()
                + ", Content=" + updatedPost.getContent());

        if (updatedPost.getStatus() == PostStatus.DRAFT) {
            boolean changed =
                    !oldTitle.equals(updatedPost.getTitle())
                            || !oldContent.equals(updatedPost.getContent());

            System.out.println("editPost: changed=" + changed);

            if (changed) {
                System.out.println("editPost: calling sendForReview for updated post ID=" + updatedPost.getId());
                sendForReview(postDTOConverter.convertToDTO(updatedPost));
            }
        }

        return postDTOConverter.convertToDTO(updatedPost);
    }

    @Override
    public void publishPost(Long postId) {
        System.out.println("publishPost called for postId=" + postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + postId + " not found."));

        post.setStatus(PostStatus.PUBLISHED);
        post.setLastModifiedDate(LocalDate.now());
        postRepository.save(post);

        System.out.println("Post published => ID=" + post.getId()
                + ", Title=" + post.getTitle()
                + ", Content=" + post.getContent());
    }

    @Override
    public List<PostDTO> getPublishedPosts() {
        System.out.println("getPublishedPosts called.");
        List<Post> publishedPosts = postRepository.findByStatus(PostStatus.PUBLISHED);
        return publishedPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getDraftPosts() {
        System.out.println("getDraftPosts called.");
        List<Post> draftPosts = postRepository.findByStatus(PostStatus.DRAFT);

        System.out.println("Number of drafts found: " + draftPosts.size());
        draftPosts.forEach(d -> System.out.println(" -> Draft ID=" + d.getId()
                + ", Title=" + d.getTitle()
                + ", Content=" + d.getContent()));
        return draftPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsFiltered(String content, String author, LocalDate createdDate, LocalDate lastModifiedDate) {
        System.out.println("getPostsFiltered called. content=" + content
                + ", author=" + author
                + ", createdDate=" + createdDate
                + ", lastModifiedDate=" + lastModifiedDate);
        List<Post> filteredPosts = postRepository.findPostsByFilters(content, author, createdDate, lastModifiedDate);
        return filteredPosts.stream()
                .map(postDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostDTO getPostById(Long id) {
        System.out.println("getPostById called. ID=" + id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found."));

        System.out.println("Found Post => Title=" + post.getTitle()
                + ", Content=" + post.getContent()
                + ", Status=" + post.getStatus());
        return postDTOConverter.convertToDTO(post);
    }
}
