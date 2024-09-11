package com.mongodbdemo.kitchensink.service;

import com.mongodbdemo.kitchensink.dto.MemberUpdateDto;
import com.mongodbdemo.kitchensink.model.Member;
import com.mongodbdemo.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class MemberService {
    private final Logger log = Logger.getLogger(getClass().getName());
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;
    @Transactional
    public void register(Member member) {
        member.setId(sequenceGeneratorService.generateSequence(Member.SEQUENCE_NAME));
        log.info(String.format("Registering %s", member.getName()));
        memberRepository.save(member);
    }

    public Member updateMember(Long id, MemberUpdateDto updatedMember) {
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Member not found"));

        // Check for duplicate email if provided and it's different
        Optional.ofNullable(updatedMember.getEmail())
                .filter(email -> !email.equals(existingMember.getEmail()))
                .ifPresent(email -> {
                    memberRepository.findByEmail(email)
                            .filter(member -> !member.getId().equals(id))
                            .ifPresent(member -> {
                                throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        "Email is already in use by another member");
                            });
                    existingMember.setEmail(email);
                });

        // Update fields only if they are not null
        Optional.ofNullable(updatedMember.getName())
                .ifPresent(existingMember::setName);
        Optional.ofNullable(updatedMember.getPhoneNumber())
                .ifPresent(existingMember::setPhoneNumber);

        return memberRepository.save(existingMember);
    }

    public void deleteMember(Long id) {
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        memberRepository.deleteById(id);
    }
}
