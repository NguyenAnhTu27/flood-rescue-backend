package com.floodrescue.module.user.service;

import com.floodrescue.module.user.dto.response.request.CreateUserAdminRequest;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity createUser(CreateUserAdminRequest request) {

        if (request.getEmail() == null && request.getPhone() == null) {
            throw new RuntimeException("Email hoặc phone phải có");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone đã tồn tại");
        }

        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("Role không tồn tại"));

        UserEntity user = UserEntity.builder()
                .role(role)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status((byte) 1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    public List<UserEntity> getUsersByRole(Long roleId) {
        // ✅ Không dùng findByIdWithRole nữa
        return userRepository.findByRole_Id(roleId);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}