package com.mongodbdemo.kitchensink.controller;

import com.mongodbdemo.kitchensink.dto.MemberUpdateDto;
import com.mongodbdemo.kitchensink.model.Member;
import com.mongodbdemo.kitchensink.repository.MemberRepository;
import com.mongodbdemo.kitchensink.service.MemberService;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MemberControllerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private Validator validator;

    @InjectMocks
    private MemberController memberController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listAllMembersSuccess() {
        // Given
        List<Member> members = Arrays.asList(new Member(), new Member());
        when(memberRepository.findAllByOrderByNameAsc()).thenReturn(members);

        // When
        ResponseEntity<Iterable<Member>> response = memberController.listAllMembers();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(members, response.getBody());
    }

    @Test
    void lookupMemberByIdFound() {
        // Given
        Member member = new Member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When
        ResponseEntity<Member> response = memberController.lookupMemberById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(member, response.getBody());
    }

    @Test
    void lookupMemberByIdNotFound() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Member> response = memberController.lookupMemberById(1L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createMemberSuccess() {
        // Given
        Member member = new Member();
        doNothing().when(memberService).register(any(Member.class));
        when(validator.validate(any(Member.class))).thenReturn(Collections.emptySet());
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createMemberConflict() {
        // Given
        Member member = new Member();
        when(validator.validate(any(Member.class))).thenReturn(Collections.emptySet());
        doThrow(new ValidationException("Email taken")).when(memberService).register(any(Member.class));

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email taken", response.getBody().get("email"));
    }

    @Test
    void createMemberBadRequest() {
        // Given
        Member member = new Member();
        when(validator.validate(any(Member.class))).thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("Unexpected error")).when(memberService).register(any(Member.class));

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().get("error"));
    }

    @Test
    void createMemberValidationErrors() {
        // Given
        Member member = new Member();
        ConstraintViolation<Member> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid email");
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(mockPath);

        Set<ConstraintViolation<Member>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Member.class))).thenReturn(violations);

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid email", response.getBody().get("email"));
    }

    @Test
    void createMemberUniqueEmailViolation() {
        // Given
        Member member = new Member();
        when(validator.validate(any(Member.class))).thenReturn(Collections.emptySet());
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void updateMemberSuccess() {
        // Given
        MemberUpdateDto updateDto = new MemberUpdateDto();
        Member updatedMember = new Member();
        when(memberService.updateMember(anyLong(), any(MemberUpdateDto.class))).thenReturn(updatedMember);

        // When
        ResponseEntity<Member> response = memberController.updateMember(1L, updateDto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedMember, response.getBody());
    }

    @Test
    void deleteMemberSuccess() {
        // Given
        doNothing().when(memberService).deleteMember(anyLong());

        // When
        ResponseEntity<Void> response = memberController.deleteMember(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void validateMemberSuccess() {
        // Given
        Member member = new Member();
        when(validator.validate(member)).thenReturn(Collections.emptySet());
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());

        // When
        memberController.createMember(member);

        // Then
        verify(validator, times(1)).validate(member);
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
    }

    @Test
    void validateMemberEmailAlreadyExists() {
        // Given
        Member member = new Member();
        when(validator.validate(member)).thenReturn(Collections.emptySet());
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void validateMemberConstraintViolationException() {
        // Given
        Member member = new Member();
        ConstraintViolation<Member> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid email");
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(mockPath);

        Set<ConstraintViolation<Member>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(member)).thenReturn(violations);

        // When
        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid email", response.getBody().get("email"));
    }
}
