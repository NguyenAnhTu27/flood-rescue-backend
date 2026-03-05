package com.floodrescue.config.seed;

import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seed roles + default coordinator account for local/dev usage.
 * Controlled by app.seed.enabled in application.properties.
 */
@Component
@RequiredArgsConstructor
public class SeedDataRunner implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.coordinator.email:coordinator@example.com}")
    private String coordinatorEmail;

    @Value("${app.seed.coordinator.phone:0900000001}")
    private String coordinatorPhone;

    @Value("${app.seed.coordinator.password:Password123}")
    private String coordinatorPassword;

    @Value("${app.seed.coordinator.full-name:Điều phối viên mặc định}")
    private String coordinatorFullName;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;

        // Ensure roles exist
        ensureRole("CITIZEN", "Công dân");
        RoleEntity coordinatorRole = ensureRole("COORDINATOR", "Điều phối");
        ensureRole("RESCUER", "Đội cứu hộ");
        ensureRole("MANAGER", "Quản lý");
        ensureRole("ADMIN", "Admin");

        // Create default coordinator user if not exists by email/phone
        boolean exists = (coordinatorEmail != null && !coordinatorEmail.isBlank() && userRepository.existsByEmail(coordinatorEmail.trim().toLowerCase()))
                || (coordinatorPhone != null && !coordinatorPhone.isBlank() && userRepository.existsByPhone(coordinatorPhone.trim()));
        if (exists) return;

        LocalDateTime now = LocalDateTime.now();

        UserEntity user = UserEntity.builder()
                .role(coordinatorRole)
                .teamId(null)
                .fullName(coordinatorFullName)
                .phone(coordinatorPhone)
                .email(coordinatorEmail == null ? null : coordinatorEmail.trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(coordinatorPassword))
                .status((byte) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(user);
    }

    private RoleEntity ensureRole(String code, String name) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> {
                    RoleEntity role = RoleEntity.builder()
                            .code(code)
                            .name(name)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return roleRepository.save(role);
                });
    }
}

