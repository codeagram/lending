package com.lending.backend.crud.service.context;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

@Service
public class SecurityContextService {

    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    public String getCurrentTenantId() {
        // Implementation depends on your tenant resolution strategy
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String tenantId = request.getHeader("X-Tenant-ID");
            if (tenantId != null)
                return tenantId;

            // Alternative: extract from subdomain, path, or JWT token
            // return extractTenantFromSubdomain(request);
        }
        return "default";
    }

    public Set<String> getCurrentUserPermissions() {
        // Implementation depends on your permission system
        // This could come from JWT token, database lookup, etc.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(java.util.stream.Collectors.toSet());
        }
        return Set.of();
    }

    public String getCurrentIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }

    public String getCurrentUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("User-Agent") : "unknown";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
