package com.finsight.api.controller;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.TransactionType;
import com.finsight.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
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

@RestController
@RequestMapping("/api/transactions")
@Validated
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService txService;

    /* ---------- READ ENDPOINTs ---------- */

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve a paginated list of all transactions for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<Page<TransactionDTO>> getAll(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(txService.getAllTransactions(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction"),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<TransactionDTO> getById(
            @Parameter(description = "Transaction ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(txService.getTransactionById(id));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type", description = "Retrieve transactions filtered by type (INCOME, EXPENSE, TRANSFER)")
    public ResponseEntity<Page<TransactionDTO>> byType(
            @Parameter(description = "Transaction type", required = true, schema = @Schema(implementation = TransactionType.class)) 
            @PathVariable TransactionType type,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(txService.getTransactionsByType(type, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieve transactions within a specific date range")
    public ResponseEntity<Page<TransactionDTO>> byDateRange(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(txService.getTransactionsByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get transactions by category", description = "Retrieve transactions filtered by category")
    public ResponseEntity<Page<TransactionDTO>> byCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(txService.getTransactionsByCategory(categoryId, pageable));
    }

    /* ---------- WRITE ENDPOINTS ---------- */

    @PostMapping
    @Operation(summary = "Create new transaction", description = "Create a new transaction for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<TransactionDTO> create(
            @Parameter(description = "Transaction data", required = true)
            @Valid @RequestBody TransactionDTO dto) {
        TransactionDTO saved = txService.createTransaction(dto);
        URI location = URI.create("/api/transactions/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Update an existing transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<TransactionDTO> update(
            @Parameter(description = "Transaction ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated transaction data", required = true)
            @Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(txService.updateTransaction(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Delete a transaction by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Transaction ID", required = true) @PathVariable Long id) {
        txService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
