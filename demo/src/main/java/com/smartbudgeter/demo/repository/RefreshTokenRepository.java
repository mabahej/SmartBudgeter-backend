package com.smartbudgeter.demo.repository;

import com.smartbudgeter.demo.model.RefreshToken;
import com.smartbudgeter.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
}