package com.lending.backend.crud.controller;

import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.crud.service.CrudService;
import com.lending.backend.common.dto.ApiResponse;
import com.lending.backend.common.dto.PagedResult;
import com.lending.backend.crud.util.SpecificationBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

public abstract class CrudController<T extends BaseEntity> {

    @Autowired
    protected CrudService<T> service;

    @Autowired
    protected SpecificationBuilder<T> specificationBuilder;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<T>>> findAll(
            @RequestParam(required = false) Map<String, String> filters,
            Pageable pageable) {

        Specification<T> spec = specificationBuilder.build(filters);
        PagedResult<T> result = service.findAll(spec, pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<T>> findById(@PathVariable Long id) {
        T entity = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<T>> create(@Valid @RequestBody T entity) {
        T created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<T>> update(
            @PathVariable Long id,
            @Valid @RequestBody T entity) {
        T updated = service.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<T>> patch(
            @PathVariable Long id,
            @RequestBody T entity) {
        T patched = service.patch(id, entity);
        return ResponseEntity.ok(ApiResponse.success(patched));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<T>>> bulkCreate(@Valid @RequestBody List<T> entities) {
        List<T> created = service.bulkCreate(entities);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }
}