package com.nimbly.phshoesbackend.services.common.core.api.rate.identity;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Extracts useful identifiers (ip, user id) from each request so we can apply scoped limits.
 */
public class RequestIdentityResolver {

    private static final String[] IP_HEADER_CANDIDATES = new String[]{
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "True-Client-IP"
    };

    public String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value)) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    public String resolveUserId(HttpServletRequest request) {
        if (request.getUserPrincipal() != null && StringUtils.hasText(request.getUserPrincipal().getName())) {
            String principalName = request.getUserPrincipal().getName();
            if (!principalName.equalsIgnoreCase("anonymousUser")) {
                return principalName;
            }
        }
        String header = request.getHeader("X-User-Id");
        if (StringUtils.hasText(header)) {
            return header.trim();
        }
        Object attr = request.getAttribute("X-User-Id");
        if (attr instanceof String attrStr && StringUtils.hasText(attrStr)) {
            return attrStr.trim();
        }
        return null;
    }
}
