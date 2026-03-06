package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.response.AuditLogResponse;
import com.floodrescue.module.admin.entity.AuditLogEntity;
import com.floodrescue.module.admin.repository.AuditLogRepository;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void log(String action, String target, String level, String detail) {
        try {
            Long actorId = resolveActorId();
            String actor = resolveActor();

            AuditLogEntity entity = AuditLogEntity.builder()
                    .actorId(actorId)
                    .action(action)
                    .entityType(resolveEntityType(target))
                    .entityId(resolveEntityId(target))
                    .oldData(null)
                    .newData(null)
                    .ipAddress(null)
                    .userAgent(null)
                    .actor(actor)
                    .target(target)
                    .level(level == null ? "INFO" : level)
                    .detail(detail)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(entity);
        } catch (Exception ignored) {
            // Do not break business flow if audit logging fails.
        }
    }

    public Map<String, Object> search(String action, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogEntity> rows = auditLogRepository.search(normalize(action), normalize(keyword), pageable);

        List<AuditLogResponse> items = rows.stream().map(a -> AuditLogResponse.builder()
                .id(a.getId())
                .action(a.getAction())
                .actor(a.getActor())
                .target(a.getTarget())
                .level(a.getLevel())
                .detail(a.getDetail())
                .createdAt(a.getCreatedAt())
                .build()).toList();

        return Map.of(
                "total", rows.getTotalElements(),
                "totalPages", rows.getTotalPages(),
                "page", page,
                "size", size,
                "items", items
        );
    }

    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }
        return auth.getName();
    }

    private Long resolveActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            try {
                Long id = Long.parseLong(name);
                if (id > 0 && userRepository.existsById(id)) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
                UserEntity byEmail = userRepository.findByEmail(name.toLowerCase()).orElse(null);
                if (byEmail != null) {
                    return byEmail.getId();
                }
            }
        }

        return userRepository.findFirstByOrderByIdAsc()
                .map(UserEntity::getId)
                .orElse(1L);
    }

    private String resolveEntityType(String target) {
        if (target == null || target.isBlank()) {
            return "SYSTEM";
        }
        int idx = target.indexOf(':');
        if (idx > 0) {
            return target.substring(0, idx).toUpperCase();
        }
        return "SYSTEM";
    }

    private Long resolveEntityId(String target) {
        if (target == null || target.isBlank()) {
            return null;
        }
        int idx = target.indexOf(':');
        if (idx < 0 || idx == target.length() - 1) {
            return null;
        }
        String candidate = target.substring(idx + 1).replaceAll("[^0-9]", "");
        if (candidate.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(candidate);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
