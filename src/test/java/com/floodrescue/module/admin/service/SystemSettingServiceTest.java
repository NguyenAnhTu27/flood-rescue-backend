package com.floodrescue.module.admin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SystemSettingService systemSettingService;

    @Test
    void getAllSystemSettingsShouldPreferValueTextWhenPresent() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(
                        Map.of("k", "legalTermsContent", "v", "Noi dung day du"),
                        Map.of("k", "footerTermsLabel", "v", "Dieu khoan su dung")
                ));

        Map<String, String> result = systemSettingService.getAllSystemSettings();

        assertThat(result)
                .containsEntry("legalTermsContent", "Noi dung day du")
                .containsEntry("footerTermsLabel", "Dieu khoan su dung");
        verify(jdbcTemplate).queryForList(
                "SELECT COALESCE(setting_key, key_name) AS k, COALESCE(NULLIF(value_text, ''), setting_value) AS v FROM system_settings"
        );
    }

    @Test
    void updateContentPagesShouldStoreLongContentInValueTextAndTrimSettingValue() {
        String longContent = "A".repeat(700);

        systemSettingService.updateContentPages(Map.of(
                "termsTitle", "Dieu khoan su dung",
                "termsContent", longContent
        ), 1L);

        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jdbcTemplate, times(12)).update(anyString(), argsCaptor.capture(), argsCaptor.capture(), argsCaptor.capture(), argsCaptor.capture(), argsCaptor.capture());

        List<Object> allArgs = argsCaptor.getAllValues();
        boolean matchedLongContentRow = false;
        for (int index = 0; index < allArgs.size(); index += 5) {
            if (!"legalTermsContent".equals(allArgs.get(index))) {
                continue;
            }

            matchedLongContentRow = true;
            assertThat(allArgs.get(index + 1)).isEqualTo(longContent.substring(0, 500));
            assertThat(allArgs.get(index + 2)).isEqualTo("legalTermsContent");
            assertThat(allArgs.get(index + 3)).isEqualTo(longContent);
            assertThat(allArgs.get(index + 4)).isEqualTo(1L);
        }

        assertThat(matchedLongContentRow).isTrue();
    }
}