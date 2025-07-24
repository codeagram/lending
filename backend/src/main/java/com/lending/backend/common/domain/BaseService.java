package com.lending.backend.common.domain;

import com.lending.backend.common.audit.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base service interface that provides common CRUD operations.
 * 
 * @param <T>  the entity type extending BaseEntity
 * @param <D>  the DTO type
 * @param <ID> the type of the entity's identifier
 */
public interface BaseService<T extends BaseEntity, D, ID extends Serializable> {
    
    D create(D dto);
    
    Optional<D> findById(ID id);
    
    Page<D> findAll(Pageable pageable);
    
    List<D> findAll();
    
    D update(ID id, D dto);
    
    void delete(ID id);
    
    boolean exists(ID id);
}
