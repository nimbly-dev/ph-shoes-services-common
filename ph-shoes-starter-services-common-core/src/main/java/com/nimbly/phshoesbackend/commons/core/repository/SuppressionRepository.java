package com.nimbly.phshoesbackend.commons.core.repository;

import com.nimbly.phshoesbackend.commons.core.model.SuppressionEntry;

public interface SuppressionRepository {

    void put(SuppressionEntry entry);

    boolean isSuppressed(String emailHash);

    void remove(String emailHash);
}

