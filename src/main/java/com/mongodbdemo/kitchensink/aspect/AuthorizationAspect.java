package com.mongodbdemo.kitchensink.aspect;

import com.mongodbdemo.kitchensink.annotation.Authorize;
import com.mongodbdemo.kitchensink.dto.AuthResponse;
import com.mongodbdemo.kitchensink.dto.AuthValidationRequestDto;
import com.mongodbdemo.kitchensink.helper.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Aspect for handling authorization based on the {@link Authorize} annotation.
 */
@Aspect
@Component
public class AuthorizationAspect {

    private static final String BEARER_PREFIX = "Bearer ";
    @Value("${auth.service.url}")
    String authServiceUrl;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Advice for methods annotated with {@link Authorize}.
     *
     * @param joinPoint the join point representing the method call
     */
    @Before("@annotation(com.mongodbdemo.kitchensink.annotation.Authorize)")
    public void authorize(JoinPoint joinPoint) {
        String[] roles = extractRoles(joinPoint);
        String token = extractToken()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        validateToken(token, roles);
    }

    /**
     * Extracts roles from the {@link Authorize} annotation.
     *
     * @param joinPoint the join point representing the method call
     * @return an array of roles
     */
    String[] extractRoles(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Authorize authorize = method.getAnnotation(Authorize.class);
        return authorize.roles();
    }

    /**
     * Extracts the token from the Authorization header.
     *
     * @return an Optional containing the token if present
     */
    Optional<String> extractToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.of(authHeader.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    /**
     * Validates the token by calling the external auth service.
     *
     * @param token the access token
     * @param roles the roles to check against
     */
    private void validateToken(String token, String[] roles) {
        AuthValidationRequestDto authValidationRequest = new AuthValidationRequestDto(token,
                List.of(roles));
        HttpEntity<AuthValidationRequestDto> entity = new HttpEntity<>(authValidationRequest,
                createHeaders());

        ResponseEntity<AuthResponse> authResponse;
        authResponse = restTemplate.exchange(
                authServiceUrl,
                HttpMethod.POST,
                entity,
                AuthResponse.class
        );
        UserContext.setUserId(authResponse.getBody().getUserId());
    }

    /**
     * Creates HTTP headers for the request.
     *
     * @return HTTP headers with content type set to JSON
     */
    HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
