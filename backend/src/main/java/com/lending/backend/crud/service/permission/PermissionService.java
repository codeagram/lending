package com.lending.backend.crud.service.permission;

import com.lending.backend.crud.service.context.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionService {

    @Autowired
    private SecurityContextService securityContextService;

    public boolean hasPermission(String permission) {
        Set<String> userPermissions = securityContextService.getCurrentUserPermissions();
        return userPermissions.contains(permission) || userPermissions.contains("*");
    }

    public void checkPermission(String permission) {
        if (!hasPermission(permission)) {
            throw new SecurityException("Access denied for permission: " + permission);
        }
    }
}
