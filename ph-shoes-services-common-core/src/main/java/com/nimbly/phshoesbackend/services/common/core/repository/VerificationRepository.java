package com.nimbly.phshoesbackend.services.common.core.repository;


import com.nimbly.phshoesbackend.services.common.core.model.VerificationEntry;

import java.util.Optional;

public interface VerificationRepository {
    void put(VerificationEntry e);
    Optional<VerificationEntry> getById(String verificationId, boolean consistentRead);
    void markUsedIfPendingAndNotExpired(String verificationId, long nowEpochSeconds);
    void markEmailVerified(String userId, String updatedAtIso);
}
