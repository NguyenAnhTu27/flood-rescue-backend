package com.floodrescue.module.publicapi.service;

import java.util.Map;

public interface RuntimeSettingsService {
    Map<String, String> getRuntimeSettings();
    Map<String, String> getContentPage(String pageKey);
}
