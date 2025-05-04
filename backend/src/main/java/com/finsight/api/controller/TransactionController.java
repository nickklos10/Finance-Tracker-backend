package com.finsight.api.controller;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.TransactionType;
import com.finsight.api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService txService;

    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(txService.getAllTransactions(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(txService.getTransactionById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> create(@Valid @RequestBody TransactionDTO dto) {
        TransactionDTO saved = txService.createTransaction(dto);
        URI location = URI.create("/api/transactions/" + saved.getId());             // << getId()
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(txService.updateTransaction(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        txService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<TransactionDTO>> byType(@PathVariable TransactionType type,
                                                       Pageable pageable) {
        return ResponseEntity.ok(txService.getTransactionsByType(type, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<TransactionDTO>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(txService.getTransactionsByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<TransactionDTO>> byCategory(@PathVariable Long categoryId,
                                                           Pageable pageable) {
        return ResponseEntity.ok(txService.getTransactionsByCategory(categoryId, pageable));
    }

}
