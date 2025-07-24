package com.lending.backend.common.domain;

import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.common.exception.ValidationException;
import com.lending.backend.common.hook.HookPhase;
import com.lending.backend.common.hook.HookRegistry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base service implementation that provides common CRUD operations.
 * 
 * @param <T>  the entity type extending BaseEntity
 * @param <D>  the DTO type
 * @param <ID> the type of the entity's identifier
 */
public abstract class BaseServiceImpl<T extends BaseEntity, D, ID extends Serializable> implements BaseService<T, D, ID> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final BaseRepository<T, ID> repository;
    protected final EntityMapper<T, D> mapper;
    protected final Validator validator;
    protected final HookRegistry hookRegistry;

    protected BaseServiceImpl(
            BaseRepository<T, ID> repository, 
            EntityMapper<T, D> mapper,
            Validator validator,
            HookRegistry hookRegistry) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.hookRegistry = hookRegistry;
    }

    @Override
    @Transactional
    public D create(D dto) {
        // Convert DTO to entity
        T entity = mapper.toEntity(dto);
        
        // Execute pre-validation hooks
        entity = executeHooks(HookPhase.PRE_VALIDATE, entity, dto);
        
        // Validate entity
        validate(entity, dto);
        
        // Execute pre-create hooks
        entity = executeHooks(HookPhase.PRE_OPERATION, entity, dto);
        
        // Save entity
        T savedEntity = repository.save(entity);
        
        // Execute post-create hooks
        executeHooks(HookPhase.POST_OPERATION, savedEntity, dto);
        
        return mapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<D> findById(ID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public D update(ID id, D dto) {
        return repository.findById(id)
                .map(existingEntity -> {
                    // Execute pre-update validation
                    T updatedEntity = mapper.updateEntity(dto, existingEntity);
                    updatedEntity = executeHooks(HookPhase.PRE_VALIDATE, updatedEntity, dto);
                    
                    // Validate entity
                    validate(updatedEntity, dto);
                    
                    // Execute pre-update hooks
                    updatedEntity = executeHooks(HookPhase.PRE_OPERATION, updatedEntity, dto);
                    
                    // Save entity
                    T savedEntity = repository.save(updatedEntity);
                    
                    // Execute post-update hooks
                    executeHooks(HookPhase.POST_OPERATION, savedEntity, dto);
                    
                    return mapper.toDto(savedEntity);
                })
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    @Override
    @Transactional
    public void delete(ID id) {
        repository.findById(id).ifPresent(entity -> {
            // Execute pre-delete hooks
            executeHooks(HookPhase.PRE_OPERATION, entity, null);
            
            // Delete entity
            repository.deleteById(id);
            
            // Execute post-delete hooks
            executeHooks(HookPhase.POST_OPERATION, entity, null);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(ID id) {
        return repository.existsById(id);
    }
    
    /**
     * Execute hooks for the given phase, entity, and DTO.
     *
     * @param phase  the hook phase
     * @param entity the entity
     * @param dto    the DTO (can be null for delete operations)
     * @return the modified entity (or the original if not modified)
     */
    @SuppressWarnings("unchecked")
    protected T executeHooks(HookPhase phase, T entity, D dto) {
        if (hookRegistry == null) {
            return entity;
        }
        return hookRegistry.executeHooks(
                (Class<T>) entity.getClass(), 
                phase, 
                entity, 
                dto
        );
    }
    
    /**
     * Validate the entity and DTO.
     *
     * @param entity the entity to validate
     * @param dto    the DTO to validate
     */
    protected void validate(T entity, D dto) {
        if (validator == null) {
            return;
        }
        
        // Validate entity
        Set<ConstraintViolation<T>> entityViolations = validator.validate(entity);
        if (!entityViolations.isEmpty()) {
            // Handle validation errors
            String errorMessage = entityViolations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + errorMessage);
        }
        
        // Validate DTO if needed
        if (dto != null) {
            Set<ConstraintViolation<D>> dtoViolations = validator.validate(dto);
            if (!dtoViolations.isEmpty()) {
                String errorMessage = dtoViolations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("DTO validation failed: " + errorMessage);
            }
        }
    }
    
    /**
     * Create multiple entities in a batch.
     *
     * @param dtos the DTOs to create
     * @return the created DTOs
     */
    @Transactional
    public List<D> createAll(List<D> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return Collections.emptyList();
        }
        
        return dtos.stream()
                .map(this::create)
                .collect(Collectors.toList());
    }
    
    /**
     * Update multiple entities in a batch.
     *
     * @param updates map of IDs to DTOs with updates
     * @return the updated DTOs
     */
    @Transactional
    public List<D> updateAll(Map<ID, D> updates) {
        if (updates == null || updates.isEmpty()) {
            return Collections.emptyList();
        }
        
        return updates.entrySet().stream()
                .map(entry -> update(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    /**
     * Delete multiple entities in a batch.
     *
     * @param ids the IDs of entities to delete
     */
    @Transactional
    public void deleteAll(List<ID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        ids.forEach(this::delete);
    }
}
