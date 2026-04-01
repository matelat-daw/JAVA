package com.futureprograms.MyIkea.Repositories.Auth;


import org.springframework.data.jpa.repository.JpaRepository;
import com.futureprograms.MyIkea.Models.Auth.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
