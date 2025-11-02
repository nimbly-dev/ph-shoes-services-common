package com.nimbly.phshoesbackend.services.common.core.repository;


import com.nimbly.phshoesbackend.services.common.core.model.VerificationEntry;
import com.nimbly.phshoesbackend.services.common.core.model.VerificationStatus;

import java.util.Optional;

public interface VerificationRepository {
    void put(VerificationEntry entry);
    Optional<VerificationEntry> getById(String verificationId, boolean consistentRead);
    void markUsedIfPendingAndNotExpired(String verificationId, long nowEpochSeconds);
    void markStatusIfPending(String verificationId, VerificationStatus newStatus);
}