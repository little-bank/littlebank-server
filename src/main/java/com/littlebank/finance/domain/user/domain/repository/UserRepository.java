package com.littlebank.finance.domain.user.domain.repository;

import com.littlebank.finance.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<User> findIdByEmail(@Param("email") String email);
    boolean existsByEmail(String email);
}
