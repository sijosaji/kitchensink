package com.mongodbdemo.kitchensink.aspect;

import com.mongodbdemo.kitchensink.helper.UserContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Aspect for handling rate limiting via a dedicated rate limit service.
 * This aspect intercepts method calls annotated with {@code @RateLimit} to enforce rate limiting
 * by calling an external rate limit service.
 */
@Aspect
@Component
public class RateLimitingAspect {

    @Value("${ratelimit.service.url}")
    private final String rateLimitServiceUrl;

    private final RestTemplate restTemplate;

    /**
     * Constructs a {@code RateLimitingAspect} with the given {@code RestTemplate} and rate limit service URL.
     *
     * @param restTemplate the {@code RestTemplate} used to make HTTP requests
     * @param rateLimitServiceUrl the URL of the rate limit service
     */
    @Autowired
    public RateLimitingAspect(RestTemplate restTemplate, @Value("${ratelimit.service.url}") String rateLimitServiceUrl) {
        this.restTemplate = restTemplate;
        this.rateLimitServiceUrl = rateLimitServiceUrl;
    }

    /**
     * Advice that runs before methods annotated with {@code @RateLimit}.
     * Retrieves the user ID from {@code UserContext}, constructs the URL for the rate limit service,
     * and calls the rate limit service to enforce rate limiting.
     *
     * @param joinPoint the join point providing context for the intercepted method call
     */
    @Before("@annotation(com.mongodbdemo.kitchensink.annotation.RateLimit)")
    public void rateLimit(JoinPoint joinPoint) {
        String userId = UserContext.getUserId();
        if (userId != null) {
            String url = buildRateLimitUrl(userId);
            HttpEntity<Void> entity = createHttpEntity();
            callRateLimitService(url, entity);
        }
        UserContext.clear();
    }

    /**
     * Constructs the URL for the rate limit service using the provided user ID.
     *
     * @param userId the ID of the user for whom the rate limit is being checked
     * @return the constructed URL as a {@code String}
     */
    String buildRateLimitUrl(String userId) {
        return rateLimitServiceUrl + "/" + userId;
    }

    /**
     * Creates an {@code HttpEntity} with empty headers for the rate limit service request.
     *
     * @return a new {@code HttpEntity} instance
     */
    HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        return new HttpEntity<>(headers);
    }

    /**
     * Calls the rate limit service with the given URL and {@code HttpEntity}.
     *
     * @param url the URL of the rate limit service
     * @param entity the {@code HttpEntity} to be sent with the request
     */
    void callRateLimitService(String url, HttpEntity<Void> entity) {
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }
}
