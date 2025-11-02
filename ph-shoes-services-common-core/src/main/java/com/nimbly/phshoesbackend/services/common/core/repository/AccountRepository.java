package com.nimbly.phshoesbackend.services.common.core.repository;


import com.nimbly.phshoesbackend.services.common.core.model.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByUserId(String userId);

    Optional<Account> findByEmailHash(String emailHash);

    boolean existsByEmailHash(String emailHash);

    void save(Account account);

    void setVerified(String userId, boolean verified);
}
