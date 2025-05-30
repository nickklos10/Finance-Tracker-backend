package com.finsight.api.repository;

import com.finsight.api.model.Transaction;
import com.finsight.api.model.TransactionType;
import com.finsight.api.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Transaction> findAll(Pageable pageable);

    /** Find all transactions for a specific user */
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Transaction> findByUser(AppUser user, Pageable pageable);

    /** Find transactions by user and type */
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Transaction> findByUserAndType(AppUser user, TransactionType type, Pageable pageable);

    /** Find transactions by user and date range */
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Transaction> findByUserAndDateBetween(AppUser user, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** Find transactions by user and category */
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Transaction> findByUserAndCategoryId(AppUser user, Long categoryId, Pageable pageable);

    /** Check if transaction belongs to user */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.id = :txId AND t.user = :user")
    boolean existsByIdAndUser(@Param("txId") Long txId, @Param("user") AppUser user);

    /** Count transactions for user */
    long countByUser(AppUser user);

    // Legacy methods - keep for backward compatibility but mark as deprecated
    @Deprecated
    @EntityGraph(attributePaths = {"category"})
    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    @Deprecated
    @EntityGraph(attributePaths = {"category"})
    Page<Transaction> findByDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Deprecated
    @EntityGraph(attributePaths = {"category"})
    Page<Transaction> findByCategoryId(Long categoryId, Pageable pageable);
}
