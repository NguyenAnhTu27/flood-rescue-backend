package com.floodrescue.config.schema;

import com.floodrescue.shared.util.StringEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LegacyUserEncryptionMigrationRunner implements ApplicationRunner {

    private static final Pattern PLAIN_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PLAIN_PHONE = Pattern.compile("^[0-9+()\\-\\s.]{6,20}$");

    private final JdbcTemplate jdbcTemplate;
    private StringEncryptor encryptor;

    @Value("${app.encryption.secret}")
    public void setEncryptionSecret(String secret) {
        this.encryptor = new StringEncryptor(secret);
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, phone, email FROM users");
        int updatedRows = 0;
        int skippedConflicts = 0;

        for (Map<String, Object> row : rows) {
            Long userId = ((Number) row.get("id")).longValue();
            String currentPhone = toNullableString(row.get("phone"));
            String currentEmail = toNullableString(row.get("email"));

            String migratedPhone = migrateField(currentPhone);
            String migratedEmail = migrateField(currentEmail);

            if (Objects.equals(currentPhone, migratedPhone) && Objects.equals(currentEmail, migratedEmail)) {
                continue;
            }

            boolean updatePhone = !Objects.equals(currentPhone, migratedPhone);
            boolean updateEmail = !Objects.equals(currentEmail, migratedEmail);

            if (updatePhone && hasConflict("phone", migratedPhone, userId)) {
                log.warn("[LegacyUserEncryption] Skip migrating phone for user {} because encrypted value would violate unique key", userId);
                updatePhone = false;
                skippedConflicts++;
            }
            if (updateEmail && hasConflict("email", migratedEmail, userId)) {
                log.warn("[LegacyUserEncryption] Skip migrating email for user {} because encrypted value would violate unique key", userId);
                updateEmail = false;
                skippedConflicts++;
            }

            if (!updatePhone && !updateEmail) {
                continue;
            }

            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder("UPDATE users SET ");
            if (updatePhone) {
                sql.append("phone = ?");
                params.add(migratedPhone);
            }
            if (updateEmail) {
                if (!params.isEmpty()) {
                    sql.append(", ");
                }
                sql.append("email = ?");
                params.add(migratedEmail);
            }
            sql.append(" WHERE id = ?");
            params.add(userId);

            try {
                jdbcTemplate.update(sql.toString(), params.toArray());
                updatedRows++;
            } catch (DuplicateKeyException ex) {
                skippedConflicts++;
                log.warn("[LegacyUserEncryption] Skip migrating user {} due to unique constraint conflict: {}", userId, ex.getMostSpecificCause().getMessage());
            }
        }

        if (updatedRows > 0) {
            log.info("[LegacyUserEncryption] Migrated {} user rows from plaintext to encrypted values", updatedRows);
        } else {
            log.info("[LegacyUserEncryption] No legacy plaintext user data found");
        }
        if (skippedConflicts > 0) {
            log.warn("[LegacyUserEncryption] Skipped {} conflicting field migrations because duplicate logical phone/email values already exist", skippedConflicts);
        }
    }

    private String migrateField(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (looksLikePlainText(value)) {
            return encryptor.encrypt(value.trim());
        }
        try {
            encryptor.decrypt(value);
            return value;
        } catch (RuntimeException ex) {
            if (containsObviouslyNonBase64Characters(value)) {
                return encryptor.encrypt(value.trim());
            }
            log.warn("[LegacyUserEncryption] Skip non-decryptable value that does not look like plaintext");
            return value;
        }
    }

    private boolean looksLikePlainText(String value) {
        String normalized = value.trim();
        return PLAIN_EMAIL.matcher(normalized).matches() || PLAIN_PHONE.matcher(normalized).matches();
    }

    private boolean containsObviouslyNonBase64Characters(String value) {
        for (char ch : value.toCharArray()) {
            boolean isBase64Char =
                    (ch >= 'A' && ch <= 'Z')
                            || (ch >= 'a' && ch <= 'z')
                            || (ch >= '0' && ch <= '9')
                            || ch == '+'
                            || ch == '/'
                            || ch == '=';
            if (!isBase64Char) {
                return true;
            }
        }
        return false;
    }

    private String toNullableString(Object rawValue) {
        return rawValue == null ? null : String.valueOf(rawValue);
    }

    private boolean hasConflict(String fieldName, String targetValue, Long currentUserId) {
        if (targetValue == null || targetValue.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE " + fieldName + " = ? AND id <> ?",
                Integer.class,
                targetValue,
                currentUserId
        );
        return count != null && count > 0;
    }
}
