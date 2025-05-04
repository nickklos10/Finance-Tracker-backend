package com.finsight.api.service;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionService {
    Page<TransactionDTO> getAllTransactions(Pageable pageable);
    TransactionDTO getTransactionById(Long id);
    TransactionDTO createTransaction(TransactionDTO dto);
    TransactionDTO updateTransaction(Long id, TransactionDTO dto);
    void deleteTransaction(Long id);

    Page<TransactionDTO> getTransactionsByType(TransactionType type, Pageable pageable);
    Page<TransactionDTO> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end,
                                                    Pageable pageable);
    Page<TransactionDTO> getTransactionsByCategory(Long categoryId, Pageable pageable);
}
