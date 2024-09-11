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
 */
@Aspect
@Component
public class RateLimitingAspect {

    @Value("${ratelimit.service.url}")
    private final String rateLimitServiceUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public RateLimitingAspect(RestTemplate restTemplate, @Value("${ratelimit.service.url}") String rateLimitServiceUrl) {
        this.restTemplate = restTemplate;
        this.rateLimitServiceUrl = rateLimitServiceUrl;
    }

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

    String buildRateLimitUrl(String userId) {
        return rateLimitServiceUrl + "/" + userId;
    }

    HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        return new HttpEntity<>(headers);
    }

    void callRateLimitService(String url, HttpEntity<Void> entity) {
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }
}
