package com.akibahub.repository;

import com.akibahub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByMemberCode(String memberCode);
}