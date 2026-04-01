package com.futureprograms.authapi.repository;

import com.futureprograms.authapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNick(String nick);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    boolean existsByNick(String nick);
}
