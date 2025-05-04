package com.finsight.api.service.impl;

import com.finsight.api.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Returns data about the user that is contained in the Access-Token (JWT)
 * sent by Auth0.  No field injection is necessary – we simply read the
 * principal that Spring Security has already placed in the thread-local
 * SecurityContext for this request.
 */
@Component
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public String getSub() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authentication in context");
        }
        Object principal = auth.getPrincipal();

        /* The resource-server creates a JwtAuthenticationToken whose
           principal is the parsed Jwt instance.  */
        if (principal instanceof Jwt jwt) {           // Java 16 pattern match
            return jwt.getSubject();                  // the “sub” claim
        }
        throw new IllegalStateException("Principal is not a Jwt");
    }
}
