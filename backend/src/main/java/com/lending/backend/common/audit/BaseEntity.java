package com.lending.backend.common.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity class that provides common fields and functionality for all JPA entities.
 * Includes:
 * - UUID v7 as primary key
 * - Audit fields (createdBy, createdAt, lastModifiedBy, lastModifiedAt)
 * - Soft delete support (deleted, deletedAt, deletedBy)
 * - Version for optimistic locking
 */
@Getter
@Setter
@MappedSuperclass
@Audited
@SQLDelete(sql = "UPDATE ${entity.name} SET deleted = true, deleted_at = NOW(), deleted_by = CURRENT_USER WHERE id = ?")
@SQLRestriction("deleted = false")
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotAudited
    private UUID id;

    @Version
    @NotAudited
    private Long version = 0L;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @PrePersist
    public void prePersist() {
        SecurityContext context = SecurityContextHolder.getContext();
        String username = "system";
        
        if (context != null && context.getAuthentication() != null && 
            context.getAuthentication().isAuthenticated()) {
            Authentication auth = context.getAuthentication();
            username = auth.getName();
        }
        
        this.createdBy = username;
        this.lastModifiedBy = username;
        this.createdAt = java.time.LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant();
        this.lastModifiedAt = java.time.LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedAt = Instant.now();
        SecurityContext context = SecurityContextHolder.getContext();
        String username = "system";
        
        if (context != null && context.getAuthentication() != null && 
            context.getAuthentication().isAuthenticated()) {
            Authentication auth = context.getAuthentication();
            username = auth.getName();
        }
        
        this.lastModifiedBy = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", version=" + version +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModifiedAt=" + lastModifiedAt +
                ", deleted=" + deleted +
                ", deletedAt=" + deletedAt +
                ", deletedBy='" + deletedBy + '\'' +
                '}';
    }
}
