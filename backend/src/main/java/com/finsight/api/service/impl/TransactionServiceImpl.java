package com.finsight.api.service.impl;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.*;
import com.finsight.api.repository.*;
import com.finsight.api.service.CurrentUserService;
import com.finsight.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Default to read-only transactions
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository txRepo;
    private final CategoryRepository    catRepo;
    private final AppUserRepository     userRepo;
    private final CurrentUserService    currentUser;

    /* -------------------------------------------------
       READ METHODS – automatically scoped to current user
       ------------------------------------------------- */

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        AppUser user = findCurrentAppUser();
        return txRepo.findByUser(user, pageable).map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public TransactionDTO getTransactionById(Long id) {
        AppUser user = findCurrentAppUser();
        return txRepo.findById(id)
                .filter(tx -> tx.getUser().equals(user))
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
    }

    /* -------------------------------------------------
       WRITE METHODS – automatically scoped to current user
       ------------------------------------------------- */

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    @Transactional // Enable writes for this method
    public TransactionDTO createTransaction(TransactionDTO dto) {
        // Use a single transaction for both user creation and transaction creation
        AppUser user = findOrCreateCurrentUserAtomically();
        Transaction tx = toEntity(dto);
        tx.setUser(user);
        
        Transaction savedTx = txRepo.save(tx);
        log.debug("Created transaction {} for user {}", savedTx.getId(), user.getAuth0Sub());
        
        return toDto(savedTx);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    @Transactional // Enable writes for this method
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        AppUser user = findCurrentAppUser();
        
        // Verify the transaction belongs to the current user
        Transaction existingTx = txRepo.findById(id)
                .filter(tx -> tx.getUser().equals(user))
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
        
        existingTx.setDescription(dto.getDescription());
        existingTx.setAmount(dto.getAmount());
        existingTx.setDate(dto.getDate());
        existingTx.setType(dto.getType());
        existingTx.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category cat = catRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + dto.getCategoryId()));
            existingTx.setCategory(cat);
        }

        Transaction savedTx = txRepo.save(existingTx);
        log.debug("Updated transaction {} for user {}", savedTx.getId(), user.getAuth0Sub());
        
        return toDto(savedTx);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    @Transactional // Enable writes for this method
    public void deleteTransaction(Long id) {
        AppUser user = findCurrentAppUser();
        
        // Verify the transaction belongs to the current user before deleting
        Transaction tx = txRepo.findById(id)
                .filter(transaction -> transaction.getUser().equals(user))
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
        
        txRepo.delete(tx);
        log.debug("Deleted transaction {} for user {}", id, user.getAuth0Sub());
    }

    /* -------------------------------------------------
       QUERY METHODS – automatically scoped to current user
       ------------------------------------------------- */

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public Page<TransactionDTO> getTransactionsByType(TransactionType type, Pageable pageable) {
        AppUser user = findCurrentAppUser();
        return txRepo.findByUserAndType(user, type, pageable).map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public Page<TransactionDTO> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end,
                                                           Pageable pageable) {
        AppUser user = findCurrentAppUser();
        return txRepo.findByUserAndDateBetween(user, start, end, pageable).map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public Page<TransactionDTO> getTransactionsByCategory(Long categoryId, Pageable pageable) {
        AppUser user = findCurrentAppUser();
        return txRepo.findByUserAndCategoryId(user, categoryId, pageable).map(this::toDto);
    }

    /* -------------------------------------------------
       MAPPING & HELPER METHODS
       ------------------------------------------------- */

    private TransactionDTO toDto(Transaction t) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(t.getId());
        dto.setDescription(t.getDescription());
        dto.setAmount(t.getAmount());
        dto.setDate(t.getDate());
        dto.setType(t.getType());
        dto.setNotes(t.getNotes());
        if (t.getCategory() != null) {
            dto.setCategoryId(t.getCategory().getId());
            dto.setCategoryName(t.getCategory().getName());
        }
        return dto;
    }

    private Transaction toEntity(TransactionDTO d) {
        Transaction tx = new Transaction();
        tx.setId(d.getId());
        tx.setDescription(d.getDescription());
        tx.setAmount(d.getAmount());
        tx.setDate(d.getDate());
        tx.setType(d.getType());
        tx.setNotes(d.getNotes());
        if (d.getCategoryId() != null) {
            Category cat = catRepo.findById(d.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + d.getCategoryId()));
            tx.setCategory(cat);
        }
        return tx;
    }

    /**
     * Finds or creates the current user within the same transaction context.
     * This ensures atomicity - if the transaction creation fails, the user creation is rolled back.
     * 
     * @return the existing or newly created AppUser
     */
    @Transactional(propagation = Propagation.MANDATORY) // Must be called within an existing transaction
    private AppUser findOrCreateCurrentUserAtomically() {
        String sub = currentUser.getSub();
        return userRepo.findByAuth0Sub(sub)
                .orElseGet(() -> {
                    // Create user with minimal information - can be enriched later via profile endpoints
                    AppUser newUser = new AppUser(null, sub, "New User", null);
                    AppUser savedUser = userRepo.save(newUser);
                    log.info("Created new user with Auth0 sub: {}", sub);
                    return savedUser;
                });
    }

    /**
     * Finds the current user, expecting them to already exist.
     * Used for operations where user should already be in the system.
     * 
     * @return the existing AppUser
     * @throws EntityNotFoundException if user doesn't exist
     */
    private AppUser findCurrentAppUser() {
        String sub = currentUser.getSub();
        return userRepo.findByAuth0Sub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + sub));
    }
}
