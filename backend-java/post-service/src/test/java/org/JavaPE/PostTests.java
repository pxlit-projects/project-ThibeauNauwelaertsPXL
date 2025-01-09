package org.JavaPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.JavaPE.client.ReviewClient;
import org.JavaPE.controller.dto.PostDTO;
import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.JavaPE.repository.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PostTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @MockBean
    private ReviewClient reviewClient;

    @Container
    private static MySQLContainer<?> sqlContainer = new MySQLContainer<>("mysql:5.7")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", sqlContainer::getDriverClassName);
    }

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
    }

    @Test
    public void testCreatePost() throws Exception {
        Mockito.when(reviewClient.hasActiveReviewForPost(Mockito.anyLong()))
                .thenReturn(true);

        Post post = new Post(
                null,
                "title",
                "content",
                "author",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks"
        );

        String postAsJson = objectMapper.writeValueAsString(post);

        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Role", "editor")
                        .content(postAsJson))
                .andExpect(status().isCreated());

        Assertions.assertEquals(1, postRepository.findAll().size());

        Mockito.verify(reviewClient, Mockito.times(1))
                .hasActiveReviewForPost(Mockito.anyLong());
    }

    /**
     * Test updating a post successfully with 'editor' role.
     */
    @Test
    public void testUpdatePostSuccess() throws Exception {
        // Arrange: Create and save a post
        Post existingPost = new Post(
                null,
                "Original Title",
                "Original Content",
                "author",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "Original remarks"
        );
        existingPost = postRepository.save(existingPost);

        // Prepare updated PostDTO
        PostDTO updatedPostDTO = new PostDTO();
        updatedPostDTO.setId(existingPost.getId());
        updatedPostDTO.setTitle("Updated Title");
        updatedPostDTO.setContent("Updated Content");
        updatedPostDTO.setRemarks("Updated remarks");

        String updatedPostAsJson = objectMapper.writeValueAsString(updatedPostDTO);

        // Mock ReviewClient behavior
        Mockito.when(reviewClient.hasActiveReviewForPost(existingPost.getId()))
                .thenReturn(false);

        // Act: Perform PUT request
        mockMvc.perform(MockMvcRequestBuilders.put("/posts/{id}", existingPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Role", "editor")
                        .content(updatedPostAsJson))
                .andExpect(status().isOk());

        // Assert: Verify the post was updated
        Post updatedPost = postRepository.findById(existingPost.getId()).orElseThrow();
        Assertions.assertEquals("Updated Title", updatedPost.getTitle());
        Assertions.assertEquals("Updated Content", updatedPost.getContent());
        Assertions.assertEquals("Updated remarks", updatedPost.getRemarks());

        // Verify ReviewClient was called
        Mockito.verify(reviewClient, Mockito.times(1))
                .hasActiveReviewForPost(existingPost.getId());
    }

    /**
     * Test updating a post without 'editor' role (should be forbidden).
     */
    @Test
    public void testUpdatePostForbidden() throws Exception {
        // Arrange: Create and save a post
        Post existingPost = new Post(
                null,
                "Original Title",
                "Original Content",
                "author",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "Original remarks"
        );
        existingPost = postRepository.save(existingPost);

        // Prepare updated PostDTO
        PostDTO updatedPostDTO = new PostDTO();
        updatedPostDTO.setId(existingPost.getId());
        updatedPostDTO.setTitle("Updated Title");
        updatedPostDTO.setContent("Updated Content");
        updatedPostDTO.setRemarks("Updated remarks");

        String updatedPostAsJson = objectMapper.writeValueAsString(updatedPostDTO);

        // Act: Perform PUT request without 'editor' role
        mockMvc.perform(MockMvcRequestBuilders.put("/posts/{id}", existingPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        // No role header or incorrect role
                        .header("X-User-Role", "viewer")
                        .content(updatedPostAsJson))
                .andExpect(status().isForbidden());

        // Assert: Verify the post was not updated
        Post postAfterUpdateAttempt = postRepository.findById(existingPost.getId()).orElseThrow();
        Assertions.assertEquals("Original Title", postAfterUpdateAttempt.getTitle());
        Assertions.assertEquals("Original Content", postAfterUpdateAttempt.getContent());
        Assertions.assertEquals("Original remarks", postAfterUpdateAttempt.getRemarks());

        // Verify ReviewClient was not called
        Mockito.verify(reviewClient, Mockito.never())
                .hasActiveReviewForPost(Mockito.anyLong());
    }

    /**
     * Test fetching published posts with 'editor' role.
     */
    @Test
    public void testGetPublishedPostsWithRole() throws Exception {
        // Arrange: Create and save published and draft posts
        Post publishedPost1 = new Post(
                null,
                "Published Title 1",
                "Published Content 1",
                "author1",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.PUBLISHED,
                "remarks1"
        );
        Post publishedPost2 = new Post(
                null,
                "Published Title 2",
                "Published Content 2",
                "author2",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.PUBLISHED,
                "remarks2"
        );
        Post draftPost = new Post(
                null,
                "Draft Title",
                "Draft Content",
                "author3",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks3"
        );
        postRepository.saveAll(List.of(publishedPost1, publishedPost2, draftPost));

        // Act: Perform GET request
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/posts/published")
                        .header("X-User-Role", "editor"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Deserialize response
        List<PostDTO> publishedPosts = objectMapper.readValue(response, new TypeReference<>() {});

        // Assert: Only published posts are returned
        Assertions.assertEquals(2, publishedPosts.size());
        Assertions.assertTrue(publishedPosts.stream().allMatch(p -> p.getStatus().equals("PUBLISHED")));
    }

    /**
     * Test fetching published posts without role (should be forbidden).
     */
    @Test
    public void testGetPublishedPostsForbidden() throws Exception {
        // Act: Perform GET request without role
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/published"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test fetching draft posts with 'editor' role.
     */
    @Test
    public void testGetDraftPostsWithRole() throws Exception {
        // Arrange: Create and save published and draft posts
        Post publishedPost = new Post(
                null,
                "Published Title",
                "Published Content",
                "author1",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.PUBLISHED,
                "remarks1"
        );
        Post draftPost1 = new Post(
                null,
                "Draft Title 1",
                "Draft Content 1",
                "author2",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks2"
        );
        Post draftPost2 = new Post(
                null,
                "Draft Title 2",
                "Draft Content 2",
                "author3",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks3"
        );
        postRepository.saveAll(List.of(publishedPost, draftPost1, draftPost2));

        // Act: Perform GET request
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/posts/drafts")
                        .header("X-User-Role", "editor"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Deserialize response
        List<PostDTO> draftPosts = objectMapper.readValue(response, new TypeReference<>() {});

        // Assert: Only draft posts are returned
        Assertions.assertEquals(2, draftPosts.size());
        Assertions.assertTrue(draftPosts.stream().allMatch(p -> p.getStatus().equals("DRAFT")));
    }

    /**
     * Test fetching draft posts without role (should be forbidden).
     */
    @Test
    public void testGetDraftPostsForbidden() throws Exception {
        // Act: Perform GET request without role
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/drafts"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test fetching filtered posts with 'editor' role and various filters.
     */
    @Test
    public void testGetFilteredPostsWithRole() throws Exception {
        // Arrange: Create and save posts
        Post post1 = new Post(
                null,
                "Java Testing",
                "Content about testing in Java",
                "author1",
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                PostStatus.PUBLISHED,
                "remarks1"
        );
        Post post2 = new Post(
                null,
                "Spring Boot Guide",
                "Content about Spring Boot",
                "author2",
                LocalDate.of(2023, 2, 1),
                LocalDate.of(2023, 2, 2),
                PostStatus.DRAFT,
                "remarks2"
        );
        Post post3 = new Post(
                null,
                "Java Concurrency",
                "Advanced topics in Java concurrency",
                "author1",
                LocalDate.of(2023, 3, 1),
                LocalDate.of(2023, 3, 2),
                PostStatus.PUBLISHED,
                "remarks3"
        );
        postRepository.saveAll(List.of(post1, post2, post3));

        // Act: Perform GET request with filters
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/posts/filtered")
                        .header("X-User-Role", "editor")
                        .param("content", "Java")
                        .param("author", "author1")
                        .param("createdDate", "2023-01-01")
                        .param("lastModifiedDate", "2023-03-02"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Deserialize response
        List<PostDTO> filteredPosts = objectMapper.readValue(response, new TypeReference<>() {});

        Assertions.assertEquals(2, filteredPosts.size());
        Assertions.assertEquals("Java Testing", filteredPosts.get(0).getTitle());
    }

    /**
     * Test fetching filtered posts without 'editor' role (should be forbidden).
     */
    @Test
    public void testGetFilteredPostsForbidden() throws Exception {
        // Act: Perform GET request without 'editor' role
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/filtered")
                        .param("content", "Java"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test fetching a published post by ID successfully.
     */
    @Test
    public void testGetPublishedPostByIdSuccess() throws Exception {
        // Arrange: Create and save a published post
        Post publishedPost = new Post(
                null,
                "Published Title",
                "Published Content",
                "author1",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.PUBLISHED,
                "remarks1"
        );
        publishedPost = postRepository.save(publishedPost);

        // Act: Perform GET request
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/posts/published/{id}", publishedPost.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Deserialize response
        PostDTO fetchedPost = objectMapper.readValue(response, PostDTO.class);

        // Assert: The fetched post matches the saved post
        Assertions.assertEquals(publishedPost.getId(), fetchedPost.getId());
        Assertions.assertEquals("Published Title", fetchedPost.getTitle());
    }

    /**
     * Test fetching a published post by ID that does not exist (should return NOT FOUND).
     */
    @Test
    public void testGetPublishedPostByIdNotFound() throws Exception {
        // Act: Perform GET request with non-existing ID
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/published/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    /**
     * Test fetching a published post by ID that is not published (should return NOT FOUND).
     */
    @Test
    public void testGetPublishedPostByIdNotPublished() throws Exception {
        // Arrange: Create and save a draft post
        Post draftPost = new Post(
                null,
                "Draft Title",
                "Draft Content",
                "author1",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks1"
        );
        draftPost = postRepository.save(draftPost);

        // Act: Perform GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/published/{id}", draftPost.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Test fetching a post by ID with 'editor' role successfully.
     */
    @Test
    public void testGetPostByIdWithRole() throws Exception {
        // Arrange: Create and save a post
        Post post = new Post(
                null,
                "Sample Title",
                "Sample Content",
                "author1",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks1"
        );
        post = postRepository.save(post);

        // Act: Perform GET request with role
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/posts/{id}", post.getId())
                        .header("X-User-Role", "editor"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Deserialize response
        PostDTO fetchedPost = objectMapper.readValue(response, PostDTO.class);

        // Assert: The fetched post matches the saved post
        Assertions.assertEquals(post.getId(), fetchedPost.getId());
        Assertions.assertEquals("Sample Title", fetchedPost.getTitle());
    }

    /**
     * Test fetching a post by ID without role (should be forbidden).
     */
    @Test
    public void testGetPostByIdForbidden() throws Exception {
        // Arrange: Create and save a post
        Post post = new Post(
                null,
                "Sample Title",
                "Sample Content",
                "author1",
                LocalDate.now(),
                LocalDate.now(),
                PostStatus.DRAFT,
                "remarks1"
        );
        post = postRepository.save(post);

        // Act: Perform GET request without role
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/{id}", post.getId()))
                .andExpect(status().isForbidden());
    }

    /**
     * Test fetching a post by ID that does not exist (should return NOT FOUND).
     */
    @Test
    public void testGetPostByIdNotFound() throws Exception {
        // Act: Perform GET request with non-existing ID
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/{id}", 999L)
                        .header("X-User-Role", "editor"))
                .andExpect(status().isNotFound());
    }
}
