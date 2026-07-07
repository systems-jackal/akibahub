package com.akibahub.user.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByIdNumber(String idNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByIdNumber(String idNumber);
}