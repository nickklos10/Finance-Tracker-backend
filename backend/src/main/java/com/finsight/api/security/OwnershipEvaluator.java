package com.finsight.api.security;

import com.finsight.api.model.AppUser;
import com.finsight.api.model.Category;
import com.finsight.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Exposes optimized boolean checks for SpEL expressions used in @PreAuthorize.
 * 
 * Note: With the updated service layer that automatically filters by user,
 * these methods are now primarily used for backwards compatibility and
 * additional security checks.
 */
@Component("ownership")
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

    private AppUser findUserBySub(String sub) {
        return userRepo.findByAuth0Sub(sub).orElse(null);
    }

    /* -------------------------------------------------
       Optimized SpEL‑called methods (must be public)
       ------------------------------------------------- */

    /** 
     * Check if user has access to transaction operations
     * Now simplified since service layer handles user filtering
     */
    public boolean checkTx(org.springframework.data.domain.Pageable pageable,
                           Authentication auth) {
        String sub = principalSub(auth);
        AppUser user = findUserBySub(sub);
        return user != null; // User exists and can access their own data
    }

    /** Check if specific Transaction belongs to caller using efficient query */
    public boolean checkTxId(Long txId, Authentication auth) {
        String sub = principalSub(auth);
        AppUser user = findUserBySub(sub);
        
        if (user == null) {
            return false;
        }
        
        // Use efficient exists query instead of loading the entire entity
        return txRepo.existsByIdAndUser(txId, user);
    }

    /** 
     * Check if Category is accessible to caller
     * Categories are currently global, but this prepares for user-specific categories
     */
    public boolean checkCategory(Long categoryId, Authentication auth) {
        String sub = principalSub(auth);
        AppUser user = findUserBySub(sub);
        
        if (user == null) {
            return false;
        }
        
        // For now, categories are global - all authenticated users can access them
        // This can be enhanced later for user-specific categories
        return catRepo.existsById(categoryId);
    }

    /**
     * Check if user can access their own profile
     */
    public boolean checkUserAccess(Authentication auth) {
        String sub = principalSub(auth);
        return findUserBySub(sub) != null;
    }
}
