package com.floodrescue.config.seed;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

/**
 * Seed roles for local/dev usage.
 * Controlled by app.seed.enabled in application.properties.
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class SeedDataRunner implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;

        // Ensure roles exist
        ensureRole("CITIZEN", "Công dân");
        ensureRole("COORDINATOR", "Điều phối");
        ensureRole("RESCUER", "Đội cứu hộ");
        ensureRole("MANAGER", "Quản lý");
        ensureRole("ADMIN", "Admin");
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
