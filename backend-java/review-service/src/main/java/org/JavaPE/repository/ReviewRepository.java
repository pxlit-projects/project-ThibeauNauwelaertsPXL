package org.JavaPE.repository;

import jakarta.transaction.Transactional;
import org.JavaPE.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Modifying
    @Transactional
    void deleteByPostIdAndStatus(Long postId, String status);

    boolean existsByPostIdAndStatus(Long postId, String pending);
}
