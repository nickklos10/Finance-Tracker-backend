package com.finsight.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 200 requests / 5 minutes per remote IP.
 * Uses only bucket4j‑core (8.10+), no deprecated APIs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)   // ensure it runs before Spring‑Security
public class IpRateLimitFilter extends OncePerRequestFilter {

    /* ---- NEW builder‑style Bandwidth ---- */
    private static final Bandwidth LIMIT = Bandwidth.builder()
            .capacity(200)                              // bucket size
            .refillGreedy(200, Duration.ofMinutes(5))   // refill strategy
            .build();                                   // 8.x DSL

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        Bucket bucket = buckets.computeIfAbsent(
                req.getRemoteAddr(),                       // one bucket per client IP
                ip -> Bucket.builder().addLimit(LIMIT).build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);                      // request is inside quota
        } else {
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType("application/json");
            res.getWriter().write("""
                { "error": "too_many_requests",
                  "detail": "Rate limit exceeded — try again later." }""");
        }
    }
}
