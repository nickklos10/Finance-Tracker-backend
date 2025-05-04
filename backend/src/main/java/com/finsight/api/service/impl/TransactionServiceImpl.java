package com.finsight.api.service.impl;

import com.finsight.api.dto.TransactionDTO;
import com.finsight.api.model.AppUser;
import com.finsight.api.model.Category;
import com.finsight.api.model.Transaction;
import com.finsight.api.model.TransactionType;
import com.finsight.api.repository.AppUserRepository;
import com.finsight.api.repository.CategoryRepository;
import com.finsight.api.repository.TransactionRepository;
import com.finsight.api.service.CurrentUserService;
import com.finsight.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository txRepo;
    private final CategoryRepository   catRepo;
    private final AppUserRepository    userRepo;
    private final CurrentUserService   currentUserService;

    /* ---------- CRUD ---------- */

    @Override
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return txRepo.findAll(pageable).map(this::toDto);
    }

    @Override
    public TransactionDTO getTransactionById(Long id) {
        return txRepo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
    }

    @Override
    public TransactionDTO createTransaction(TransactionDTO dto) {
        // Ensure AppUser exists
        String sub = currentUserService.getSub();
        AppUser user = userRepo.findByAuth0Sub(sub)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setAuth0Sub(sub);
                    return userRepo.save(newUser);
                });

        // Map DTO to entity, set user, save
        Transaction tx = toEntity(dto);
        tx.setUser(user);
        Transaction saved = txRepo.save(tx);
        return toDto(saved);
    }

    @Override
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        if (!txRepo.existsById(id)) {
            throw new EntityNotFoundException("Transaction not found: " + id);
        }
        dto.setId(id);
        Transaction tx = toEntity(dto);
        // Preserve user association
        AppUser user = findCurrentAppUser();
        tx.setUser(user);
        return toDto(txRepo.save(tx));
    }

    @Override
    public void deleteTransaction(Long id) {
        if (!txRepo.existsById(id)) {
            throw new EntityNotFoundException("Transaction not found: " + id);
        }
        txRepo.deleteById(id);
    }

    /* ---------- queries ---------- */

    @Override
    public Page<TransactionDTO> getTransactionsByType(TransactionType type, Pageable pageable) {
        return txRepo.findByType(type, pageable).map(this::toDto);
    }

    @Override
    public Page<TransactionDTO> getTransactionsByDateRange(LocalDateTime start,
                                                           LocalDateTime end,
                                                           Pageable pageable) {
        return txRepo.findByDateBetween(start, end, pageable).map(this::toDto);
    }

    @Override
    public Page<TransactionDTO> getTransactionsByCategory(Long categoryId, Pageable pageable) {
        return txRepo.findByCategoryId(categoryId, pageable).map(this::toDto);
    }

    /* ---------- mapping helpers ---------- */

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

    /** Helper to fetch current AppUser for updates */
    private AppUser findCurrentAppUser() {
        String sub = currentUserService.getSub();
        return userRepo.findByAuth0Sub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + sub));
    }
}
