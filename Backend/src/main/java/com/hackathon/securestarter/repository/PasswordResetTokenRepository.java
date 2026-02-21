package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.PasswordResetToken;
import com.hackathon.securestarter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserAndIsUsedFalse(User user);

    void deleteByExpiryDateLessThan(LocalDateTime now);

    void deleteByUser(User user);

}