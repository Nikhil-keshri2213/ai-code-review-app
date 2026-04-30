package com.aicodereview.storage.repository;

import com.aicodereview.common.enums.Severity;
import com.aicodereview.storage.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByRepositoryAndPrNumber(
            String repository, Integer prNumber);

    List<Review> findByRepositoryAndPrNumberAndSeverity(
            String repository, Integer prNumber, Severity severity);

    long countByRepositoryAndPrNumber(
            String repository, Integer prNumber);

    long countByRepositoryAndPrNumberAndSeverity(
            String repository, Integer prNumber, Severity severity);

    Page<Review> findByRepositoryOrderByCreatedAtDesc(
            String repository, Pageable pageable);

    @Query("SELECT DISTINCT r.prNumber FROM Review r " +
           "WHERE r.repository = :repo ORDER BY r.prNumber DESC")
    List<Integer> findDistinctPrNumbersByRepository(
            @Param("repo") String repository);
}