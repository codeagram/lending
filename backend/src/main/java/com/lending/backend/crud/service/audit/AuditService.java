package com.lending.backend.crud.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.crud.entity.AuditTrail;
import com.lending.backend.crud.repository.AuditTrailRepository;
import com.lending.backend.crud.service.context.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditTrailRepository auditRepository;

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private ObjectMapper objectMapper;

    @Async("asyncExecutor")
    public void logCreate(String entityName, String entityId, BaseEntity entity) {
        try {
            AuditTrail audit = createBaseAudit(entityName, entityId, AuditTrail.Operation.CREATE);
            audit.setNewValues(objectMapper.writeValueAsString(entity));
            auditRepository.save(audit);
        } catch (JsonProcessingException e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to log audit for create: " + e.getMessage());
        }
    }

    @Async("asyncExecutor")
    public void logUpdate(String entityName, String entityId, BaseEntity oldEntity, BaseEntity newEntity) {
        try {
            AuditTrail audit = createBaseAudit(entityName, entityId, AuditTrail.Operation.UPDATE);
            audit.setOldValues(objectMapper.writeValueAsString(oldEntity));
            audit.setNewValues(objectMapper.writeValueAsString(newEntity));
            auditRepository.save(audit);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to log audit for update: " + e.getMessage());
        }
    }

    @Async("asyncExecutor")
    public void logDelete(String entityName, String entityId, BaseEntity entity) {
        try {
            AuditTrail audit = createBaseAudit(entityName, entityId, AuditTrail.Operation.DELETE);
            audit.setOldValues(objectMapper.writeValueAsString(entity));
            auditRepository.save(audit);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to log audit for delete: " + e.getMessage());
        }
    }

    @Async("asyncExecutor")
    public void logView(String entityName, String entityId, BaseEntity entity) {
        AuditTrail audit = createBaseAudit(entityName, entityId, AuditTrail.Operation.VIEW);
        auditRepository.save(audit);
    }

    @Async("asyncExecutor")
    public void logBulkCreate(String entityName, List<? extends BaseEntity> entities) {
        entities.forEach(entity -> logCreate(entityName, entity.getId().toString(), entity));
    }

    @Async("asyncExecutor")
    public void logBulkView(String entityName, int count) {
        AuditTrail audit = createBaseAudit(entityName, "bulk", AuditTrail.Operation.VIEW);
        audit.setNewValues("Viewed " + count + " records");
        auditRepository.save(audit);
    }

    private AuditTrail createBaseAudit(String entityName, String entityId, AuditTrail.Operation operation) {
        AuditTrail audit = new AuditTrail();
        audit.setEntityName(entityName);
        audit.setEntityId(entityId);
        audit.setOperation(operation);
        audit.setUserId(securityContextService.getCurrentUserId());
        audit.setIpAddress(securityContextService.getCurrentIpAddress());
        audit.setUserAgent(securityContextService.getCurrentUserAgent());
        audit.setTenantId(securityContextService.getCurrentTenantId());
        return audit;
    }
}