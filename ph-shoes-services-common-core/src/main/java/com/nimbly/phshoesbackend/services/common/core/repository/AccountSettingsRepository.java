package com.nimbly.phshoesbackend.services.common.core.repository;

import java.util.Optional;

public interface AccountSettingsRepository {
    Optional<String> getSettingsJson(String userId);

    void putSettingsJson(String userId, String settingsJson);
}
