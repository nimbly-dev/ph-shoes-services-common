package com.nimbly.phshoesbackend.services.common.core.repository;

import com.nimbly.phshoesbackend.services.common.core.model.SuppressionEntry;

public interface SuppressionRepository {

    void put(SuppressionEntry entry);

    boolean isSuppressed(String emailHash);

    void remove(String emailHash);
}

