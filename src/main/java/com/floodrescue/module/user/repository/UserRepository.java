package com.floodrescue.module.user.repository;

import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<UserEntity> findFirstByOrderByIdAsc();

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    // Query theo roleId
    List<UserEntity> findByRole_Id(Long roleId);

    // Phân trang theo Role
    Page<UserEntity> findByRole(RoleEntity role, Pageable pageable);

    // Search user theo keyword + role + phân trang
    @Query("""
SELECT u FROM UserEntity u
WHERE
(:keyword IS NULL OR
LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
STR(u.id) LIKE CONCAT('%', :keyword, '%'))
AND (:roleId IS NULL OR u.role.id = :roleId)
""")
    Page<UserEntity> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            Pageable pageable
    );

    // Đếm số user theo status
    long countByStatus(Byte status);

    long countByRole_Id(Long roleId);

}
