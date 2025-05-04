package com.finsight.api.repository;

import com.finsight.api.model.Transaction;
import com.finsight.api.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Find all transactions of a given type (INCOME, EXPENSE, TRANSFER) */
    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    /** Find all transactions whose `date` is between start (inclusive) and end (inclusive) */
    Page<Transaction> findByDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** Find all transactions belonging to the given category ID */
    Page<Transaction> findByCategoryId(Long categoryId, Pageable pageable);
}
