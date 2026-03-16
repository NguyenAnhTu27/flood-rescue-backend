package com.floodrescue.module.auth.repository;

import com.floodrescue.module.auth.entity.AuthRefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshTokenEntity, Long> {

    Optional<AuthRefreshTokenEntity> findByTokenHash(String tokenHash);

    List<AuthRefreshTokenEntity> findByUserIdAndRevokedFalse(Long userId);
}
