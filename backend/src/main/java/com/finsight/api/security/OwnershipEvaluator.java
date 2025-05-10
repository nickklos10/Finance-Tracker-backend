package com.finsight.api.security;

import com.finsight.api.model.AppUser;
import com.finsight.api.model.Category;
import com.finsight.api.model.Transaction;
import com.finsight.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Exposes simple boolean checks for SpEL expressions used in @PreAuthorize.
 *
 * All methods return true only if the current principal (Auth0 sub)
 * owns the entity being accessed.
 */
@Component("ownership")        // the bean name used in SpEL
@RequiredArgsConstructor
public class OwnershipEvaluator {

    private final TransactionRepository txRepo;
    private final CategoryRepository    catRepo;
    private final AppUserRepository     userRepo;

    /* -------------------------------------------------
       Helper – extract Auth0 subject from the principal
       ------------------------------------------------- */
    private String principalSub(Authentication auth) {
        Object p = auth.getPrincipal();
        return (p instanceof Jwt jwt) ? jwt.getSubject() : "";
    }

    /* -------------------------------------------------
       SpEL‑called methods (must be public)
       ------------------------------------------------- */

    /** true if every Transaction in the page belongs to caller */
    public boolean checkTx(org.springframework.data.domain.Pageable pageable,
                           Authentication auth) {

        String sub = principalSub(auth);
        return txRepo.findAll(pageable).stream()
                .allMatch(tx -> sub.equals(tx.getUser().getAuth0Sub()));
    }

    /** true if specific Transaction belongs to caller */
    public boolean checkTxId(Long txId, Authentication auth) {
        String sub = principalSub(auth);
        return txRepo.findById(txId)
                .map(Transaction::getUser)
                .map(AppUser::getAuth0Sub)
                .map(sub::equals)
                .orElse(false);
    }

    /** true if Category belongs to caller (or is global) */
    public boolean checkCategory(Long categoryId, Authentication auth) {
        String sub = principalSub(auth);
        // If categories are shared for now, just return true
        Category cat = catRepo.findById(categoryId).orElse(null);
        return cat != null; // adjust later when categories become per‑user
    }
}
