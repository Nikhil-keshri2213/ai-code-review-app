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

    @Query(value = """
            SELECT
                CASE
                    WHEN file_name LIKE '%.java' THEN 'java'
                    WHEN file_name LIKE '%.py'   THEN 'python'
                    WHEN file_name LIKE '%.js'   THEN 'javascript'
                    WHEN file_name LIKE '%.ts'   THEN 'typescript'
                    WHEN file_name LIKE '%.go'   THEN 'go'
                    WHEN file_name LIKE '%.rs'   THEN 'rust'
                    WHEN file_name LIKE '%.kt'   THEN 'kotlin'
                    WHEN file_name LIKE '%.sql'  THEN 'sql'
                    WHEN file_name LIKE '%.sh'   THEN 'shell'
                    WHEN file_name LIKE '%.cpp'  THEN 'cpp'
                    WHEN file_name LIKE '%.md'   THEN 'markdown'
                    ELSE 'other'
                END AS language,
                COUNT(*) AS issue_count
            FROM reviews
            WHERE repository = :repo
            GROUP BY language
            ORDER BY issue_count DESC
            """, nativeQuery = true)
    List<Object[]> countByLanguageForRepo(@Param("repo") String repository);
}