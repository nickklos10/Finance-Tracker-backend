package com.finsight.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
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
 * Configurable rate limiting filter using bucket4j‑core.
 * Default: 200 requests / 5 minutes per remote IP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private volatile Bandwidth cachedBandwidth;

    @PostConstruct
    private void initializeBandwidth() {
        int requestsPerWindow = appProperties.getRateLimit().getRequestsPerWindow();
        int windowMinutes = appProperties.getRateLimit().getWindowMinutes();
        this.cachedBandwidth = Bandwidth.builder()
                .capacity(requestsPerWindow)
                .refillGreedy(requestsPerWindow, Duration.ofMinutes(windowMinutes))
                .build();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        Bucket bucket = buckets.computeIfAbsent(
                req.getRemoteAddr(),
               ip -> Bucket.builder().addLimit(cachedBandwidth).build());
        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            int requestsPerWindow = appProperties.getRateLimit().getRequestsPerWindow();
            int windowMinutes = appProperties.getRateLimit().getWindowMinutes();
            
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType("application/json");
            res.getWriter().write(String.format("""
                { "error": "too_many_requests",
                  "detail": "Rate limit exceeded — %d requests per %d minutes. Try again later.",
                  "retry_after_minutes": %d }""", 
                requestsPerWindow, windowMinutes, windowMinutes));
        }
    }
}
