package com.mongodbdemo.kitchensink.controller;

import com.mongodbdemo.kitchensink.dto.MemberUpdateDto;
import com.mongodbdemo.kitchensink.model.Member;
import com.mongodbdemo.kitchensink.repository.MemberRepository;
import com.mongodbdemo.kitchensink.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
public class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberRepository repository;

    @Mock
    private MemberService memberService;

    private Member sampleMember;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        sampleMember = new Member();
        sampleMember.setId(1L);
        sampleMember.setName("John Doe");
        sampleMember.setEmail("john.doe@example.com");
        sampleMember.setPhoneNumber("1234567890");
    }

    @Test
    public void testListAllMembers() {
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(sampleMember));

        ResponseEntity<Iterable<Member>> response = memberController.listAllMembers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().iterator().hasNext());
    }

    @Test
    public void testLookupMemberByIdFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleMember));

        ResponseEntity<Member> response = memberController.lookupMemberById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleMember, response.getBody());
    }

    @Test
    public void testLookupMemberByIdNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Member> response = memberController.lookupMemberById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testCreateMemberSuccess() {
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        doNothing().when(memberService).register(sampleMember);

        ResponseEntity<Map<String, String>> response = memberController.createMember(sampleMember);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCreateMemberEmailTaken() {
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleMember));

        ResponseEntity<Map<String, String>> response = memberController.createMember(sampleMember);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(Map.of("email", "Email taken"), response.getBody());
    }

    @Test
    public void testCreateMemberBadRequest() {
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Error")).when(memberService).register(sampleMember);

        ResponseEntity<Map<String, String>> response = memberController.createMember(sampleMember);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "Error"), response.getBody());
    }

    @Test
    public void testUpdateMember() {
        MemberUpdateDto updateDto = new MemberUpdateDto();
        updateDto.setName("Jane Doe");

        when(memberService.updateMember(1L, updateDto)).thenReturn(sampleMember);

        ResponseEntity<Member> response = memberController.updateMember(1L, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleMember, response.getBody());
    }

    @Test
    public void testDeleteMember() {
        doNothing().when(memberService).deleteMember(1L);

        ResponseEntity<Void> response = memberController.deleteMember(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testCreateMemberBadRequest2() throws Exception {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("invalid-email"); // Invalid email format
        member.setPhoneNumber("1234567890");

        // Simulate a generic exception during creation
        when(repository.findByEmail(member.getEmail())).thenThrow(new RuntimeException("Generic error"));

        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("error", "Generic error");

        ResponseEntity<Map<String, String>> response = memberController.createMember(member);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }


}


