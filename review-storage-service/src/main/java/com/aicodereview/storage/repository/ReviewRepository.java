package com.aicodereview.storage.repository;

import com.aicodereview.storage.entity.Review;
import com.aicodereview.common.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByRepositoryAndPrNumber(String repository, Integer prNumber);

    List<Review> findByRepositoryAndPrNumberAndSeverity(
            String repository, Integer prNumber, Severity severity);

    long countByRepositoryAndPrNumber(String repository, Integer prNumber);
}