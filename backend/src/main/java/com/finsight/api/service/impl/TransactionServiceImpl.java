package com.finsight.api.service.impl;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.*;
import com.finsight.api.repository.*;
import com.finsight.api.service.CurrentUserService;
import com.finsight.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository txRepo;
    private final CategoryRepository    catRepo;
    private final AppUserRepository     userRepo;
    private final CurrentUserService    currentUser;

    /* -------------------------------------------------
       READ METHODS – caller must own the data
       ------------------------------------------------- */

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app') and @ownership.checkTx(#pageable, principal)")
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return txRepo.findAll(pageable).map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app') and @ownership.checkTxId(#id, principal)")
    public TransactionDTO getTransactionById(Long id) {
        return txRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
    }

    /* -------------------------------------------------
       WRITE METHODS – caller must own the data
       ------------------------------------------------- */

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public TransactionDTO createTransaction(TransactionDTO dto) {
        AppUser user = findOrCreateCurrentUser();
        Transaction tx = toEntity(dto);
        tx.setUser(user);
        return toDto(txRepo.save(tx));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app') and @ownership.checkTxId(#id, principal)")
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        dto.setId(id);
        Transaction tx = toEntity(dto);
        tx.setUser(findCurrentAppUser());
        return toDto(txRepo.save(tx));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app') and @ownership.checkTxId(#id, principal)")
    public void deleteTransaction(Long id) {
        txRepo.deleteById(id);
    }

    /* -------------------------------------------------
       QUERY METHODS – always scoped to caller
       ------------------------------------------------- */

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public Page<TransactionDTO> getTransactionsByType(TransactionType type, Pageable pageable) {
        return txRepo.findByType(type, pageable).map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app')")
    public Page<TransactionDTO> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end,
                                                           Pageable pageable) {
        return txRepo.findByDateBetween(start, end, pageable).map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_fin:app') and @ownership.checkCategory(#categoryId, principal)")
    public Page<TransactionDTO> getTransactionsByCategory(Long categoryId, Pageable pageable) {
        return txRepo.findByCategoryId(categoryId, pageable).map(this::toDto);
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

    private AppUser findOrCreateCurrentUser() {
        String sub = currentUser.getSub();
        return userRepo.findByAuth0Sub(sub)
                .orElseGet(() -> userRepo.save(new AppUser(null, sub, null, null)));
    }

    private AppUser findCurrentAppUser() {
        String sub = currentUser.getSub();
        return userRepo.findByAuth0Sub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + sub));
    }
}
