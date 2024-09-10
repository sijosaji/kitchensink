package com.mongodbdemo.kitchensink.repository;

import com.mongodbdemo.kitchensink.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, Long> {
    List<Member> findAllByOrderByNameAsc();

    Optional<Member> findByEmail(String email);
}
