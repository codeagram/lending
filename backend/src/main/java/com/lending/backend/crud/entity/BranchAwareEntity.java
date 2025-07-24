package com.lending.backend.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Entity
public abstract class BranchAwareEntity extends SoftDeleteEntity {
    @Column(name = "branch_id")
    private String branchId;
}