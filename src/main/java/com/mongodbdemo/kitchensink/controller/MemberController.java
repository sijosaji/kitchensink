package com.mongodbdemo.kitchensink.controller;

import com.mongodbdemo.kitchensink.annotation.Authorize;
import com.mongodbdemo.kitchensink.annotation.RateLimit;
import com.mongodbdemo.kitchensink.dto.MemberUpdateDto;
import com.mongodbdemo.kitchensink.model.Member;
import com.mongodbdemo.kitchensink.repository.MemberRepository;
import com.mongodbdemo.kitchensink.service.MemberService;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.mongodbdemo.kitchensink.MemberConstants.*;

/**
 * Controller for managing members.
 */
@RestController
@RequestMapping(BASE_PATH + MEMBERS_PATH)
public class MemberController {

    private static final Logger LOG = Logger.getLogger(MemberController.class.getName());

    @Autowired
    private MemberRepository repository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private Validator validator;

    /**
     * Retrieves a list of all members, ordered by name in ascending order.
     *
     * @return a list of all members
     */
    @Authorize(roles = {ROLE_MEMBERS_READ})
    @RateLimit
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<Member>> listAllMembers() {
        return ResponseEntity.ok(repository.findAllByOrderByNameAsc());
    }

    /**
     * Retrieves a member by its ID.
     *
     * @param id the ID of the member
     * @return the member if found, or a 404 Not Found response if not
     */
    @Authorize(roles = {ROLE_MEMBERS_READ})
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Member> lookupMemberById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new member.
     *
     * @param member the member to be created
     * @return a 200 Created response if successful, or a 409 Conflict if the email is already taken,
     *         or a 400 Bad Request for other errors
     */
    @Authorize(roles = {ROLE_MEMBERS_WRITE})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createMember(@RequestBody  Member member) {
        try {
            validateMember(member);
            memberService.register(member);
            return ResponseEntity.ok().build();
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            return createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("email", "Email taken"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Updates an existing member.
     *
     * @param id             the ID of the member to be updated
     * @param updatedMember  the updated member details
     * @return the updated member
     */
    @Authorize(roles = {ROLE_MEMBERS_WRITE})
    @PatchMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Member> updateMember(@PathVariable Long id,
                                               @Valid @RequestBody MemberUpdateDto updatedMember) {
        Member member = memberService.updateMember(id, updatedMember);
        return ResponseEntity.ok(member);
    }

    /**
     * Deletes a member by its ID.
     *
     * @param id the ID of the member to be deleted
     * @return a 204 No Content response if successful
     */
    @Authorize(roles = {ROLE_MEMBERS_DELETE})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates the member to ensure the email is unique.
     *
     * @param member the member to be validated
     * @throws ValidationException if the email is already taken
     */
    private void validateMember(Member member) throws ValidationException {
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }

        if (emailAlreadyExists(member.getEmail())) {
            throw new ValidationException("Unique Email Violation");
        }
    }

    /**
     * Checks if the email already exists in the repository.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    private boolean emailAlreadyExists(String email) {
        return repository.findByEmail(email).isPresent();
    }

    private ResponseEntity<Map<String, String>> createViolationResponse(Set<ConstraintViolation<?>> violations) {
        LOG.fine("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObj);
    }
}
