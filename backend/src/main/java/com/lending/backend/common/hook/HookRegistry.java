package com.lending.backend.common.hook;

import com.lending.backend.common.audit.BaseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing entity hooks.
 */
@Component
public class HookRegistry {

    private final Map<Class<?>, List<EntityHook<?, ?>>> hooks = new ConcurrentHashMap<>();

    /**
     * Register a hook for a specific entity type.
     *
     * @param hook the hook to register
     * @param <T>  the entity type
     * @param <D>  the DTO type
     */
    public <T extends BaseEntity, D> void registerHook(EntityHook<T, D> hook) {
        Class<T> entityClass = hook.getEntityClass();
        hooks.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(hook);
    }

    /**
     * Get all hooks for a specific entity type.
     *
     * @param entityClass the entity class
     * @param <T>         the entity type
     * @param <D>         the DTO type
     * @return list of hooks for the entity type
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEntity, D> List<EntityHook<T, D>> getHooks(Class<T> entityClass) {
        // This cast is safe because we control the types that go into and out of the map
        List<EntityHook<?, ?>> entityHooks = hooks.getOrDefault(entityClass, new ArrayList<>());
        return (List<EntityHook<T, D>>) (List<?>) entityHooks;
    }

    /**
     * Execute hooks for a specific phase.
     *
     * @param entityClass the entity class
     * @param phase       the hook phase
     * @param entity      the entity
     * @param dto         the DTO (can be null for delete operations)
     * @param <T>         the entity type
     * @param <D>         the DTO type
     * @return the modified entity (or the original if not modified)
     */
    public <T extends BaseEntity, D> T executeHooks(Class<T> entityClass, HookPhase phase, T entity, D dto) {
        List<EntityHook<T, D>> entityHooks = getHooks(entityClass);
        T result = entity;

        for (EntityHook<T, D> hook : entityHooks) {
            switch (phase) {
                case PRE_VALIDATE:
                    result = hook.preValidate(result, dto);
                    break;
                case PRE_OPERATION:
                    if (dto != null) {
                        if (entity.getId() == null) {
                            result = hook.preCreate(result, dto);
                        } else {
                            result = hook.preUpdate(result, dto);
                        }
                    } else {
                        hook.preDelete(result);
                    }
                    break;
                case POST_OPERATION:
                    if (dto != null) {
                        if (entity.getId() == null) {
                            hook.postCreate(result, dto);
                        } else {
                            hook.postUpdate(result, dto);
                        }
                    } else {
                        hook.postDelete(result);
                    }
                    break;
                case AFTER_COMMIT:
                    // Handle after commit logic if needed
                    break;
            }
        }

        return result;
    }
}
