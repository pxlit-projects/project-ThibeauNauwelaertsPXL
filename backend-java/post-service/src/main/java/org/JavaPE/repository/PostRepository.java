package org.JavaPE.repository;

import org.JavaPE.domain.Post;
import org.JavaPE.domain.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.status = :status")
    List<Post> findByStatus(@Param("status") PostStatus status);

    @Query("SELECT p FROM Post p WHERE " +
            "(:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(p.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:startDate IS NULL OR p.createdDate >= :startDate) AND " +
            "(:endDate IS NULL OR p.createdDate <= :endDate)")
    List<Post> findPostsByFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}