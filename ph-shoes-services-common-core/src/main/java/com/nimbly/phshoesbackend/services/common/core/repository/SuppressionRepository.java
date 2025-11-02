package com.nimbly.phshoesbackend.services.common.core.repository;


import com.nimbly.phshoesbackend.services.common.core.model.SuppressionEntry;

public interface SuppressionRepository {
    boolean isSuppressed(String emailHash);
    void put(SuppressionEntry entry);
    void remove(String emailHash);
    SuppressionEntry get(String emailHash);
}