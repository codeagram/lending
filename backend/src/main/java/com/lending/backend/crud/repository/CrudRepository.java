package com.lending.backend.crud.repository;

import com.lending.backend.crud.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface CrudRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false AND e.id = :id")
    Optional<T> findActiveById(@Param("id") Long id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false")
    Page<T> findAllActive(Pageable pageable);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.deletedAt = :deletedAt, e.deletedBy = :deletedBy WHERE e.id = :id")
    void softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt,
            @Param("deletedBy") String deletedBy);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false AND e.tenantId = :tenantId")
    List<T> findAllActiveByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false AND e.tenantId = :tenantId")
    Page<T> findAllActiveByTenant(@Param("tenantId") String tenantId, Pageable pageable);
}