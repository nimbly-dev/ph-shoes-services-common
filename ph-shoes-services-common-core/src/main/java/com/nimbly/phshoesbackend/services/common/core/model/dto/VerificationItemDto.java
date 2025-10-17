package com.nimbly.phshoesbackend.services.common.core.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class VerificationItemDto {
    private String  id;
    private String  userId;
    private String  emailPlain;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant usedAt;
    private Long    ttl;

}
