package com.lending.backend.modules.user.entity;

import com.lending.backend.common.audit.BaseEntity;
import com.lending.backend.crud.annotations.CrudEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@CrudEntity(value = "User", path = "/api/users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String mobile;

    @Column(nullable = false)
    private String passwordHash;
}