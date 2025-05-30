package com.finsight.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.TransactionType;
import com.finsight.api.service.TransactionService;
import com.finsight.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    private TransactionDTO sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new TransactionDTO();
        sampleTransaction.setId(1L);
        sampleTransaction.setDescription("Test Transaction");
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setDate(LocalDateTime.now());
        sampleTransaction.setType(TransactionType.EXPENSE);
        sampleTransaction.setCategoryId(1L);
        sampleTransaction.setCategoryName("Food");
        sampleTransaction.setNotes("Test notes");
    }

    @Test
    void getAllTransactions_ShouldReturnPagedResults() throws Exception {
        Page<TransactionDTO> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionService.getAllTransactions(any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Test Transaction"));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction() throws Exception {
        when(transactionService.getTransactionById(anyLong()))
                .thenReturn(sampleTransaction);

        mockMvc.perform(get("/api/transactions/1")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Transaction"));
    }

    @Test
    void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
        when(transactionService.createTransaction(any(TransactionDTO.class)))
                .thenReturn(sampleTransaction);

        mockMvc.perform(post("/api/transactions")
                        .with(jwt().authorities(() -> "fin:app"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Transaction"));
    }

    @Test
    void updateTransaction_ShouldReturnUpdatedTransaction() throws Exception {
        when(transactionService.updateTransaction(anyLong(), any(TransactionDTO.class)))
                .thenReturn(sampleTransaction);

        mockMvc.perform(put("/api/transactions/1")
                        .with(jwt().authorities(() -> "fin:app"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Transaction"));
    }

    @Test
    void deleteTransaction_ShouldReturnNoContent() throws Exception {
        doNothing().when(transactionService).deleteTransaction(anyLong());

        mockMvc.perform(delete("/api/transactions/1")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTransactionsByType_ShouldReturnFilteredResults() throws Exception {
        Page<TransactionDTO> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionService.getTransactionsByType(any(TransactionType.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions/type/EXPENSE")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"));
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnFilteredResults() throws Exception {
        Page<TransactionDTO> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), 
                any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions/date-range")
                        .param("startDate", "2023-01-01T00:00:00")
                        .param("endDate", "2023-12-31T23:59:59")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createTransaction_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        TransactionDTO invalidTransaction = new TransactionDTO();
        // Missing required fields

        mockMvc.perform(post("/api/transactions")
                        .with(jwt().authorities(() -> "fin:app"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTransaction)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessWithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }
} 