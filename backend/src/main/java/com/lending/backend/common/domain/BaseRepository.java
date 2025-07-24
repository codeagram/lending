package com.lending.backend.common.domain;

import com.lending.backend.common.audit.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface that provides common CRUD operations for all entities.
 * 
 * @param <T>  the entity type extending BaseEntity
 * @param <ID> the type of the entity's identifier
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID extends Serializable> 
    extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
    // Common repository methods can be added here
}
