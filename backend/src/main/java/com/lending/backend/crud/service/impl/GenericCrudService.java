package com.lending.backend.crud.service.impl;

import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.crud.repository.CrudRepository;
import com.lending.backend.crud.service.CrudService;
import com.lending.backend.crud.service.audit.AuditService;
import com.lending.backend.crud.service.cache.CacheService;
import com.lending.backend.crud.service.context.SecurityContextService;
import com.lending.backend.crud.service.hook.EntityHookService;
import com.lending.backend.crud.service.permission.PermissionService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

@Service("crudServiceImpl")
public class GenericCrudService<T extends BaseEntity> extends CrudService<T> {

    private final Class<T> entityClass;
    private final String entityName;

    public GenericCrudService(
            CrudRepository<T> repository,
            AuditService auditService,
            CacheService cacheService,
            EntityHookService hookService,
            PermissionService permissionService,
            SecurityContextService securityContextService,
            Class<T> entityClass,
            String entityName) {
        super(repository, auditService, cacheService, hookService, permissionService, securityContextService);
        this.entityClass = entityClass;
        this.entityName = entityName;
    }

    @Override
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    protected String getEntityName() {
        return entityName;
    }

    @Override
    protected void validateEntity(T entity) {
        // Placeholder for validation logic
        // You can add Bean Validation, custom checks, etc.
    }

    @Override
    protected void updateEntityFields(T existing, T updated) {
        copyNonNullProperties(updated, existing);
    }

    @Override
    protected void patchEntityFields(T existing, T partial) {
        copyNonNullProperties(partial, existing);
    }

    @Override
    protected T cloneEntity(T entity) {
        try {
            T clone = entityClass.getDeclaredConstructor().newInstance();
            copyNonNullProperties(entity, clone);
            return clone;
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone entity", e);
        }
    }

    private void copyNonNullProperties(T source, T target) {
        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to copy properties", e);
            }
        }
    }
}
