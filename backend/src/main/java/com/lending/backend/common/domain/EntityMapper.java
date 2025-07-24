package com.lending.backend.common.domain;

import com.lending.backend.common.audit.BaseEntity;

/**
 * Interface for mapping between entities and DTOs.
 * 
 * @param <T> the entity type extending BaseEntity
 * @param <D> the DTO type
 */
public interface EntityMapper<T extends BaseEntity, D> {
    
    /**
     * Convert a DTO to an entity.
     * 
     * @param dto the DTO to convert
     * @return the converted entity
     */
    T toEntity(D dto);
    
    /**
     * Convert an entity to a DTO.
     * 
     * @param entity the entity to convert
     * @return the converted DTO
     */
    D toDto(T entity);
    
    /**
     * Update an entity from a DTO.
     * 
     * @param dto the DTO with updated values
     * @param entity the entity to update
     * @return the updated entity
     */
    default T updateEntity(D dto, T entity) {
        // Default implementation does nothing - should be overridden by concrete mappers
        return entity;
    }
}
