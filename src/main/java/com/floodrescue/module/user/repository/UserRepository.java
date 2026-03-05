package com.floodrescue.module.user.repository;

import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.role WHERE u.id = :id")
    Optional<UserEntity> findByIdWithRole(@Param("id") Long id);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<UserEntity> findByRole(RoleEntity role);

    // ✅ Thêm dòng này để query theo roleId trực tiếp
    List<UserEntity> findByRole_Id(Long roleId);
}