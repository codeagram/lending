package com.lending.backend.crud.service;

import com.lending.backend.common.dto.PagedResult;
import com.lending.backend.common.exception.ResourceNotFoundException;
import com.lending.backend.crud.annotations.MultiTenant;
import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.crud.entity.BranchAwareEntity;
import com.lending.backend.crud.repository.CrudRepository;
import com.lending.backend.crud.service.audit.AuditService;
import com.lending.backend.crud.service.cache.CacheService;
import com.lending.backend.crud.service.context.SecurityContextService;
import com.lending.backend.crud.service.hook.EntityHookService;
import com.lending.backend.crud.service.permission.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class CrudService<T extends BaseEntity> {

    // Dependencies
    protected final CrudRepository<T> repository;
    protected final AuditService auditService;
    protected final CacheService cacheService;
    protected final EntityHookService hookService;
    protected final PermissionService permissionService;
    protected final SecurityContextService securityContextService;

    // Abstract methods
    protected abstract Class<T> getEntityClass();

    protected abstract String getEntityName();

    protected abstract void validateEntity(T entity);

    protected abstract void updateEntityFields(T existing, T updated);

    protected abstract void patchEntityFields(T existing, T partial);

    protected abstract T cloneEntity(T entity);

    public CrudService(CrudRepository<T> repository, AuditService auditService, CacheService cacheService,
            EntityHookService hookService, PermissionService permissionService,
            SecurityContextService securityContextService) {
        this.repository = repository;
        this.auditService = auditService;
        this.cacheService = cacheService;
        this.hookService = hookService;
        this.permissionService = permissionService;
        this.securityContextService = securityContextService;
    }

    @Transactional(readOnly = true)
    public PagedResult<T> findAll(Specification<T> spec, Pageable pageable) {
        checkPermission("view");

        String cacheKey = generateCacheKey("findAll", spec.hashCode(), pageable.hashCode());
        // Use the new CacheService which returns an Optional
        @SuppressWarnings("unchecked")
        PagedResult<T> cachedResult = (PagedResult<T>) cacheService.get(getEntityName(), cacheKey, PagedResult.class)
                .orElse(null);
        if (cachedResult != null) {
            return cachedResult;
        }

        Specification<T> finalSpec = applySecurityFilters(spec);
        Page<T> page = repository.findAll(finalSpec, pageable);

        auditService.logBulkView(getEntityName(), page.getNumberOfElements());

        PagedResult<T> result = new PagedResult<>(page);
        cacheService.put(getEntityName(), cacheKey, result);
        return result;
    }

    @Transactional(readOnly = true)
    public T findById(Long id) {
        checkPermission("view");

        String cacheKey = generateCacheKey("findById", id);
        // Use the new CacheService which returns an Optional
        T cachedEntity = cacheService.get(getEntityName(), cacheKey, getEntityClass()).orElse(null);
        if (cachedEntity != null) {
            return cachedEntity;
        }

        T entity = findEntityById(id);
        auditService.logView(getEntityName(), id.toString(), entity);
        cacheService.put(getEntityName(), cacheKey, entity);
        return entity;
    }

    @Transactional
    public T create(T entity) {
        checkPermission("create");
        prepareEntityForCreation(entity);
        validateEntity(entity);

        hookService.executeBeforeCreate(getEntityName(), entity);
        T savedEntity = repository.save(entity);
        executePostCreateActions(savedEntity);

        return savedEntity;
    }

    @Transactional
    public T update(Long id, T entityUpdateData) {
        checkPermission("edit");

        T existingEntity = findEntityById(id);
        T oldEntityForHook = cloneEntity(existingEntity);
        validateEntity(entityUpdateData);

        hookService.executeBeforeUpdate(getEntityName(), existingEntity, entityUpdateData);
        updateEntityFields(existingEntity, entityUpdateData);
        T savedEntity = repository.save(existingEntity);
        executePostUpdateActions(oldEntityForHook, savedEntity);

        return savedEntity;
    }

    @Transactional
    public T patch(Long id, T partialEntityData) {
        checkPermission("edit");

        T existingEntity = findEntityById(id);
        T oldEntityForHook = cloneEntity(existingEntity);

        hookService.executeBeforeUpdate(getEntityName(), existingEntity, partialEntityData);
        patchEntityFields(existingEntity, partialEntityData);
        T savedEntity = repository.save(existingEntity);
        executePostUpdateActions(oldEntityForHook, savedEntity);

        return savedEntity;
    }

    @Transactional
    public void delete(Long id) {
        checkPermission("delete");

        T entity = findEntityById(id);
        hookService.executeBeforeDelete(getEntityName(), entity);

        if (isSoftDeleteEnabled()) {
            performSoftDelete(entity);
        } else {
            repository.delete(entity);
        }

        executePostDeleteActions(entity);
    }

    @Transactional
    public List<T> bulkCreate(List<T> entities) {
        checkPermission("create");

        entities.forEach(entity -> {
            prepareEntityForCreation(entity);
            validateEntity(entity);
            hookService.executeBeforeCreate(getEntityName(), entity);
        });

        List<T> savedEntities = repository.saveAll(entities);
        auditService.logBulkCreate(getEntityName(), savedEntities);

        savedEntities.forEach(this::executePostCreateActions);

        return savedEntities;
    }

    // === PROTECTED & PRIVATE HELPERS ===

    private T findEntityById(Long id) {
        Specification<T> spec = (root, query, cb) -> hasId(id).toPredicate(root, query, cb);
        spec = applySecurityFilters(spec);

        return repository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), "id", id));
    }

    private void checkPermission(String action) {
        String permission = getEntityName().toLowerCase() + ":" + action;
        if (!permissionService.hasPermission(permission)) {
            throw new AccessDeniedException(
                    "Access denied for action '" + action + "' on entity '" + getEntityName() + "'");
        }
    }

    private void prepareEntityForCreation(T entity) {
        if (isMultiTenant() && entity instanceof BranchAwareEntity) {
            ((BranchAwareEntity) entity).setBranchId(securityContextService.getCurrentTenantId());
        }
    }

    protected boolean isSoftDeleteEnabled() {
        return true; // Default to soft delete as per requirements
    }

    protected Specification<T> includeDeleted(Specification<T> spec) {
        // If soft delete is enabled, modify the spec to include only non-deleted items
        if (isSoftDeleteEnabled()) {
            return (root, query, cb) -> {
                if (spec == null) {
                    return cb.isFalse(root.get("isDeleted"));
                }
                return cb.and(
                        spec.toPredicate(root, query, cb),
                        cb.isFalse(root.get("isDeleted")));
            };
        }
        return spec;
    }

    private void performSoftDelete(T entity) {
        entity.setDeleted(true);
        entity.setDeletedAt(java.time.Instant.now());
        entity.setDeletedBy(securityContextService.getCurrentUserId());

        T savedEntity = repository.save(entity);
        auditService.logDelete(getEntityName(), entity.getId().toString(), savedEntity);

        // Clear relevant caches
        cacheService.evict(getEntityName(), generateCacheKey("findById", entity.getId()));
        clearEntityCache();
    }

    private void executePostCreateActions(T newEntity) {
        auditService.logCreate(getEntityName(), newEntity.getId().toString(), newEntity);
        hookService.executeAfterCreate(getEntityName(), newEntity, false); // Sync
        hookService.executeAfterCreate(getEntityName(), newEntity, true); // Async
        clearEntityCache();
    }

    private void executePostUpdateActions(T oldEntity, T newEntity) {
        auditService.logUpdate(getEntityName(), newEntity.getId().toString(), oldEntity, newEntity);
        hookService.executeAfterUpdate(getEntityName(), oldEntity, newEntity, false); // Sync
        hookService.executeAfterUpdate(getEntityName(), oldEntity, newEntity, true); // Async
        clearEntityCache();
    }

    private void executePostDeleteActions(T deletedEntity) {
        auditService.logDelete(getEntityName(), deletedEntity.getId().toString(), deletedEntity);
        hookService.executeAfterDelete(getEntityName(), deletedEntity, false); // Sync
        hookService.executeAfterDelete(getEntityName(), deletedEntity, true); // Async
        clearEntityCache();
    }

    private boolean isMultiTenant() {
        return getEntityClass().isAnnotationPresent(MultiTenant.class);
    }

    // isSoftDeleteEnabled() is already defined above

    private String generateCacheKey(String operation, Object... params) {
        StringBuilder key = new StringBuilder(operation);
        if (isMultiTenant()) {
            key.append(":tenant=").append(securityContextService.getCurrentTenantId());
        }
        for (Object param : params) {
            key.append(":").append(param.toString());
        }
        return key.toString();
    }

    private void clearEntityCache() {
        // Use the new clear method from CacheService
        cacheService.clear(getEntityName());
    }

    private Specification<T> applySecurityFilters(Specification<T> spec) {
        Specification<T> finalSpec = spec;
        if (isSoftDeleteEnabled()) {
            finalSpec = finalSpec.and(isNotDeleted());
        }
        if (isMultiTenant()) {
            finalSpec = finalSpec.and(belongsToTenant(securityContextService.getCurrentTenantId()));
        }
        return finalSpec;
    }

    // === SPECIFICATION HELPERS ===

    private Specification<T> hasId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    private Specification<T> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    private Specification<T> belongsToTenant(String tenantId) {
        return (root, query, cb) -> cb.equal(root.get("branchId"), tenantId);
    }
}