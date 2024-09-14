package com.mongodbdemo.kitchensink.aspect;

import com.mongodbdemo.kitchensink.annotation.Authorize;
import com.mongodbdemo.kitchensink.controller.MemberController;

import com.mongodbdemo.kitchensink.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
@SpringBootTest
public class AuthorizationAspectTest {

    @InjectMocks
    private AuthorizationAspect authorizationAspect;

    @Mock
    private HttpServletRequest request;

    private String authServiceUrl = "http://test-auth-service.com";

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authorizationAspect.authServiceUrl = "http://test-auth-service.com";
    }

    @Test
    public void authorizeWithValidTokenAndRolesShouldCallValidateToken() throws NoSuchMethodException {
        // Arrange
        String token = "valid-token";
        String[] roles = {"ROLE_USER"};
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        Method method = MemberController.class.getMethod("listAllMembers");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));


        when(restTemplate.exchange(eq(authServiceUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthResponse.class))).thenReturn(ResponseEntity.ok(new AuthResponse()));

        Authorize authorize = method.getAnnotation(Authorize.class);

        // Act
        authorizationAspect.authorize(joinPoint);

        // Assert
        verify(restTemplate, times(1)).exchange(
                eq(authServiceUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthResponse.class)
        );
    }

    @Test
    public void authorizeWithMissingTokenShouldThrowUnauthorizedException() throws NoSuchMethodException {
        // Arrange
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        // Create a Method object representing a method annotated with Authorize
        Method method = MemberController.class.getMethod("listAllMembers");

        // Mock the behavior of MethodSignature to return the Method
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);

        // Create a mock Authorize annotation and set up roles
        Authorize authorize = mock(Authorize.class);
        when(authorize.roles()).thenReturn(new String[]{"MEMBERS:READ"});

        // Mock the behavior of Method to return the mock Authorize annotation
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> authorizationAspect.authorize(joinPoint));
    }

    @Test
    public void authorizeWithInvalidTokenShouldThrowUnauthorizedException() throws NoSuchMethodException {
        // Arrange
        String token = "invalid-token";
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        Method method = MemberController.class.getMethod("listAllMembers");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        Authorize authorize = method.getAnnotation(Authorize.class);

        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)).when(restTemplate).exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        );

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> authorizationAspect.authorize(joinPoint));
    }

    @Test
    public void extractRolesShouldReturnRolesFromAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = MemberController.class.getMethod("listAllMembers");
        Authorize authorize = method.getAnnotation(Authorize.class);

        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);

        // Act
        String[] roles = authorizationAspect.extractRoles(joinPoint);

        // Assert
        assertArrayEquals(new String[]{"MEMBERS:READ"}, roles);
    }

    @Test
    public void extractTokenWithHeaderPresentAndValidShouldReturnToken() {
        // Arrange
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Optional<String> extractedToken = authorizationAspect.extractToken();

        // Assert
        assertTrue(extractedToken.isPresent());
        assertEquals(token, extractedToken.get());
    }

    @Test
    public void extractTokenWithHeaderAbsentShouldReturnEmpty() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        Optional<String> extractedToken = authorizationAspect.extractToken();

        // Assert
        assertFalse(extractedToken.isPresent());
    }

    @Test
    public void createHeadersShouldReturnHeadersWithContentType() {
        // Act
        HttpHeaders headers = authorizationAspect.createHeaders();

        // Assert
        assertNotNull(headers);
        assertEquals("application/json", headers.getContentType().toString());
    }

    @Test
    void testExtractTokenWithBearerToken() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + "token123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Optional<String> token = authorizationAspect.extractToken();

        // Assert
        assertEquals(Optional.of("token123"), token);
    }

    @Test
    void testExtractTokenWithoutBearerToken() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Optional<String> token = authorizationAspect.extractToken();

        // Assert
        assertEquals(Optional.empty(), token);
    }

    @Test
    void testExtractTokenWithInvalidPrefix() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Optional<String> token = authorizationAspect.extractToken();

        // Assert
        assertEquals(Optional.empty(), token);
    }
}
