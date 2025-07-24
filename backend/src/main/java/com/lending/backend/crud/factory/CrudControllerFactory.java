package com.lending.backend.crud.factory;

import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.crud.annotations.CrudEntity;
import com.lending.backend.crud.controller.CrudController;
import com.lending.backend.crud.service.CrudService;
import com.lending.backend.crud.util.SpecificationBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Configuration
public class CrudControllerFactory {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @PostConstruct
    public void registerControllers() {
        Map<String, Object> entities = context.getBeansWithAnnotation(CrudEntity.class);
        for (Object entityObj : entities.values()) {
            Class<?> entityClass = entityObj.getClass();
            CrudEntity annotation = entityClass.getAnnotation(CrudEntity.class);
            String path = annotation.path();
            registerControllerForEntity(entityClass, path);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseEntity> void registerControllerForEntity(Class<?> entityClass, String path) {
        try {
            Class<T> clazz = (Class<T>) entityClass;
            CrudService<T> service = (CrudService<T>) context.getBean("crudServiceImpl");
            SpecificationBuilder<T> specBuilder = (SpecificationBuilder<T>) context.getBean(SpecificationBuilder.class);

            CrudController<T> controller = new CrudController<>() {
                {
                    this.service = service;
                    this.specificationBuilder = specBuilder;
                }
            };

            String beanName = clazz.getSimpleName() + "Controller";
            beanFactory.registerSingleton(beanName, controller);
            handlerMapping.afterPropertiesSet(); // force refresh routes

        } catch (Exception e) {
            throw new RuntimeException("Failed to register controller for " + entityClass.getSimpleName(), e);
        }
    }
}
