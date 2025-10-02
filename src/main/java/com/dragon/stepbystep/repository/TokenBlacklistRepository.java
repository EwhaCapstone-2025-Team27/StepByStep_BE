package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByTokenHash(String tokenHash);
    boolean existsByTokenHash(String tokenHash);
    void deleteByExpiresAtBefore(Instant now);
}