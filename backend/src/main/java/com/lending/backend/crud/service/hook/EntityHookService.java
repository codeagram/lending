package com.lending.backend.crud.service.hook;

import com.lending.backend.crud.annotations.*;
import com.lending.backend.common.audit.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EntityHookService {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, Object> entityServices = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Method>> hookMethods = new ConcurrentHashMap<>();

    public void executeBeforeCreate(String entityName, BaseEntity entity) {
        executeHook(entityName, "beforeCreate", entity);
    }

    public void executeAfterCreate(String entityName, BaseEntity entity, boolean async) {
        if (async) {
            executeAfterCreateAsync(entityName, entity);
        } else {
            executeHook(entityName, "afterCreate", entity);
        }
    }

    public void executeBeforeUpdate(String entityName, BaseEntity existing, BaseEntity updated) {
        executeHook(entityName, "beforeUpdate", existing, updated);
    }

    public void executeAfterUpdate(String entityName, BaseEntity oldEntity, BaseEntity newEntity, boolean async) {
        if (async) {
            executeAfterUpdateAsync(entityName, oldEntity, newEntity);
        } else {
            executeHook(entityName, "afterUpdate", oldEntity, newEntity);
        }
    }

    public void executeBeforeDelete(String entityName, BaseEntity entity) {
        executeHook(entityName, "beforeDelete", entity);
    }

    public void executeAfterDelete(String entityName, BaseEntity entity, boolean async) {
        if (async) {
            executeAfterDeleteAsync(entityName, entity);
        } else {
            executeHook(entityName, "afterDelete", entity);
        }
    }

    @Async("asyncExecutor")
    public void executeAfterCreateAsync(String entityName, BaseEntity entity) {
        executeAsyncHook(entityName, "afterCreate", entity);
    }

    @Async("asyncExecutor")
    public void executeAfterUpdateAsync(String entityName, BaseEntity oldEntity, BaseEntity newEntity) {
        executeAsyncHook(entityName, "afterUpdate", oldEntity, newEntity);
    }

    @Async("asyncExecutor")
    public void executeAfterDeleteAsync(String entityName, BaseEntity entity) {
        executeAsyncHook(entityName, "afterDelete", entity);
    }

    private void executeHook(String entityName, String hookType, Object... args) {
        Object service = getEntityService(entityName);
        if (service == null)
            return;

        Method method = getHookMethod(entityName, hookType, false);
        if (method != null) {
            try {
                method.invoke(service, args);
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute " + hookType + " hook for " + entityName, e);
            }
        }
    }

    private void executeAsyncHook(String entityName, String hookType, Object... args) {
        Object service = getEntityService(entityName);
        if (service == null)
            return;

        Method method = getHookMethod(entityName, hookType, true);
        if (method != null) {
            try {
                method.invoke(service, args);
            } catch (Exception e) {
                // Log error but don't fail for async hooks
                System.err.println(
                        "Failed to execute async " + hookType + " hook for " + entityName + ": " + e.getMessage());
            }
        }
    }

    private Object getEntityService(String entityName) {
        return entityServices.computeIfAbsent(entityName, this::findEntityService);
    }

    private Object findEntityService(String entityName) {
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(EntityService.class);
        for (Object service : services.values()) {
            EntityService annotation = service.getClass().getAnnotation(EntityService.class);
            if (entityName.equals(annotation.value())) {
                return service;
            }
        }
        return null;
    }

    private Method getHookMethod(String entityName, String hookType, boolean async) {
        String key = entityName + ":" + hookType + ":" + async;
        return hookMethods.computeIfAbsent(entityName, k -> new HashMap<>())
                .computeIfAbsent(key, k -> findHookMethod(entityName, hookType, async));
    }

    private Method findHookMethod(String entityName, String hookType, boolean async) {
        Object service = getEntityService(entityName);
        if (service == null)
            return null;

        Class<? extends Annotation> annotationClass = getAnnotationClass(hookType);
        if (annotationClass == null)
            return null;

        for (Method method : service.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                boolean methodAsync = isAsyncMethod(method, annotationClass);
                if (methodAsync == async) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private Class<? extends Annotation> getAnnotationClass(String hookType) {
        switch (hookType) {
            case "beforeCreate":
                return BeforeCreate.class;
            case "afterCreate":
                return AfterCreate.class;
            case "beforeUpdate":
                return BeforeUpdate.class;
            case "afterUpdate":
                return AfterUpdate.class;
            case "beforeDelete":
                return BeforeDelete.class;
            case "afterDelete":
                return AfterDelete.class;
            default:
                return null;
        }
    }

    private boolean isAsyncMethod(Method method, Class<?> annotationClass) {
        if (annotationClass == AfterCreate.class) {
            return method.getAnnotation(AfterCreate.class).async();
        } else if (annotationClass == AfterUpdate.class) {
            return method.getAnnotation(AfterUpdate.class).async();
        } else if (annotationClass == AfterDelete.class) {
            return method.getAnnotation(AfterDelete.class).async();
        }
        return false;
    }
}