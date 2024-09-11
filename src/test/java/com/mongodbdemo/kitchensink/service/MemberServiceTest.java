package com.mongodbdemo.kitchensink.service;

import com.mongodbdemo.kitchensink.dto.MemberUpdateDto;
import com.mongodbdemo.kitchensink.model.Member;
import com.mongodbdemo.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterMember() {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john.doe@example.com");
        member.setPhoneNumber("1234567890");

        when(sequenceGeneratorService.generateSequence(Member.SEQUENCE_NAME)).thenReturn(1L);

        memberService.register(member);

        verify(sequenceGeneratorService, times(1)).generateSequence(Member.SEQUENCE_NAME);
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    public void testUpdateMemberSuccess() {
        Long memberId = 1L;
        Member existingMember = new Member();

        Member updatedMember = new Member();

        updatedMember.setName("Jane Doe");
        updatedMember.setEmail("jane.doe@example.com");
        updatedMember.setPhoneNumber("1234567890");


        existingMember.setId(memberId);
        existingMember.setName("John Doe");
        existingMember.setEmail("john.doe@example.com");
        existingMember.setPhoneNumber("1234567890");

        MemberUpdateDto updateDto = new MemberUpdateDto();
        updateDto.setName("Jane Doe");
        updateDto.setEmail("jane.doe@example.com");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        when(memberRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMember);
        Member updatedMemberFromDb = memberService.updateMember(memberId, updateDto);

        assertEquals("Jane Doe", updatedMemberFromDb.getName());
        assertEquals("jane.doe@example.com", updatedMemberFromDb.getEmail());
        verify(memberRepository, times(1)).save(existingMember);
    }

    @Test
    public void testUpdateMemberEmailDuplicate() {
        Long memberId = 1L;
        Member existingMember = new Member();
        existingMember.setId(memberId);
        existingMember.setName("John Doe");
        existingMember.setEmail("john.doe@example.com");

        MemberUpdateDto updateDto = new MemberUpdateDto();
        updateDto.setEmail("duplicate@example.com");

        Member duplicateMember = new Member();
        duplicateMember.setId(2L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        when(memberRepository.findByEmail("duplicate@example.com"))
                .thenReturn(Optional.of(duplicateMember));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            memberService.updateMember(memberId, updateDto);
        });

        assertEquals(HttpStatus.CONFLICT, thrown.getStatusCode());
        assertEquals("Email is already in use by another member", thrown.getReason());
    }

    @Test
    public void testUpdateMemberNotFound() {
        Long memberId = 1L;
        MemberUpdateDto updateDto = new MemberUpdateDto();

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            memberService.updateMember(memberId, updateDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Member not found", thrown.getReason());
    }

    @Test
    public void testDeleteMemberSuccess() {
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(new Member()));

        memberService.deleteMember(memberId);

        verify(memberRepository, times(1)).deleteById(memberId);
    }

    @Test
    public void testDeleteMemberNotFound() {
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            memberService.deleteMember(memberId);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Member not found", thrown.getReason());
    }
}
