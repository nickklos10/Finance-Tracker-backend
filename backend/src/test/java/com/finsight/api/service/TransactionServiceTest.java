package com.finsight.api.service;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.AppUser;
import com.finsight.api.model.Category;
import com.finsight.api.model.Transaction;
import com.finsight.api.model.TransactionType;
import com.finsight.api.repository.AppUserRepository;
import com.finsight.api.repository.CategoryRepository;
import com.finsight.api.repository.TransactionRepository;
import com.finsight.api.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository txRepo;
    
    @Mock
    private CategoryRepository catRepo;
    
    @Mock
    private AppUserRepository userRepo;
    
    @Mock
    private CurrentUserService currentUser;
    
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AppUser testUser;
    private Category testCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new AppUser(1L, "auth0|test123", "Test User", "test@example.com");
        testCategory = new Category(1L, "Test Category", "Test Description");
        testTransaction = new Transaction(
                1L,
                "Test Transaction",
                BigDecimal.valueOf(100.00),
                LocalDateTime.now(),
                TransactionType.EXPENSE,
                testCategory,
                "Test notes",
                testUser
        );
    }

    @Test
    void shouldGetAllTransactionsForUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(testTransaction));
        
        when(currentUser.getSub()).thenReturn("auth0|test123");
        when(userRepo.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(testUser));
        when(txRepo.findByUser(eq(testUser), eq(pageable))).thenReturn(transactionPage);

        // When
        Page<TransactionDTO> result = transactionService.getAllTransactions(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Test Transaction");
        assertThat(result.getContent().get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void shouldCreateTransactionForCurrentUser() {
        // Given
        TransactionDTO dto = new TransactionDTO();
        dto.setDescription("New Transaction");
        dto.setAmount(BigDecimal.valueOf(50.00));
        dto.setDate(LocalDateTime.now());
        dto.setType(TransactionType.INCOME);
        dto.setCategoryId(1L);

        when(currentUser.getSub()).thenReturn("auth0|test123");
        when(userRepo.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(testUser));
        when(catRepo.findById(1L)).thenReturn(Optional.of(testCategory));
        when(txRepo.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionService.createTransaction(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Test Transaction");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(currentUser.getSub()).thenReturn("auth0|nonexistent");
        when(userRepo.findByAuth0Sub("auth0|nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getAllTransactions(PageRequest.of(0, 10)))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }
} 