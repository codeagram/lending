package com.lending.backend.common.hook;

import com.lending.backend.common.audit.BaseEntity;

/**
 * Interface for entity lifecycle hooks.
 *
 * @param <T> the entity type
 * @param <D> the DTO type
 */
public interface EntityHook<T extends BaseEntity, D> {

    /**
     * Called before an entity is validated.
     *
     * @param entity the entity to be validated
     * @param dto    the DTO being processed
     * @return the modified entity (or a new instance)
     */
    default T preValidate(T entity, D dto) {
        return entity;
    }

    /**
     * Called before an entity is created.
     *
     * @param entity the entity to be created
     * @param dto    the DTO being processed
     * @return the modified entity (or a new instance)
     */
    default T preCreate(T entity, D dto) {
        return entity;
    }

    /**
     * Called after an entity is created.
     *
     * @param entity the created entity
     * @param dto    the DTO that was used
     */
    default void postCreate(T entity, D dto) {
        // Default implementation does nothing
    }

    /**
     * Called before an entity is updated.
     *
     * @param entity the entity to be updated
     * @param dto    the DTO with new values
     * @return the modified entity (or a new instance)
     */
    default T preUpdate(T entity, D dto) {
        return entity;
    }

    /**
     * Called after an entity is updated.
     *
     * @param entity the updated entity
     * @param dto    the DTO that was used
     */
    default void postUpdate(T entity, D dto) {
        // Default implementation does nothing
    }

    /**
     * Called before an entity is deleted.
     *
     * @param entity the entity to be deleted
     */
    default void preDelete(T entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is deleted.
     *
     * @param entity the deleted entity
     */
    default void postDelete(T entity) {
        // Default implementation does nothing
    }

    /**
     * Get the entity class this hook is for.
     *
     * @return the entity class
     */
    Class<T> getEntityClass();
}
