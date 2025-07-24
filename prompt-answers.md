## ✅ 1. **Overview of the Base Layer**

This base layer is meant to standardize and streamline CRUD operations, soft deletion, auditing, role-based access control (RBAC), and hooks for business logic across all entities. It consists of:

| Layer                 | Purpose                                                                |
| --------------------- | ---------------------------------------------------------------------- |
| `BaseEntity`          | Unified entity metadata: ID, audit fields, soft delete, versioning     |
| `BaseRepository`      | Generic JPA repository with soft delete awareness                      |
| `BaseService`         | Interface with common service operations (CRUD + bulk + import/export) |
| `PermissionEvaluator` | ABAC/RBAC-driven access check at method level                          |
| `EntityHook`          | Business logic lifecycle events (pre/post create/update/delete)        |
| `HookRegistry`        | Runtime registry to attach hooks to specific entities                  |
| `ServiceImpl`         | Concrete class to bind everything together using Spring DI             |

---

## 🧠 2. **How It Works**

### 2.1 `BaseEntity`

* All entities extend `BaseEntity`.
* Includes:

  * `UUID` ID using `@UuidGenerator`
  * Audit fields (`createdAt`, `createdBy`, etc.) — filled by Spring Audit
  * `version` for optimistic locking
  * `deleted`, `deletedAt`, `deletedBy` for soft delete
  * Uses Hibernate Envers for versioning

### 2.2 `BaseRepository`

* Generic interface using `JpaRepository` and `JpaSpecificationExecutor`.
* Overrides `findAll()` to auto-filter soft-deleted records.
* `softDelete()` method marks a record as deleted instead of removing it.
* `findById()` also respects soft-deleted records.

### 2.3 `BaseService`

* Generic interface that defines:

  * CRUD
  * Bulk create/update/delete
  * Search (with custom criteria)
  * Import/export (CSV/Excel/JSON etc.)

### 2.4 `PermissionEvaluator` + `@CheckPermission`

* Fine-grained access control for API methods using annotations.
* Generates permission strings like `loan:read`, `loan:create`.
* Matches against user's `GrantedAuthority` list.

### 2.5 `EntityHook` and `HookRegistry`

* Allows registering logic without polluting services.
* Hooks can be defined per entity to intercept lifecycle events.
* HookRegistry allows dynamic registration/discovery.

### 2.6 Concrete Service Layer

* Implements `BaseServiceImpl<T, D, ID>` with all logic.
* Connects:

  * `Repository`
  * `Mapper`
  * `Hooks`

---

## ✅ 3. **Windsurf Acceptance Criteria**

### ✅ 3.1 `BaseEntity`

| Criteria        | Requirement                                  |
| --------------- | -------------------------------------------- |
| UUID generation | Uses `@UuidGenerator(style = TIME)`          |
| Auditing        | Uses `@CreatedBy`, `@CreatedDate`, etc.      |
| Soft Delete     | Includes `deleted`, `deletedAt`, `deletedBy` |
| Versioning      | Includes `@Version`                          |
| Extensibility   | All entities must extend `BaseEntity`        |

### ✅ 3.2 `BaseRepository`

| Criteria              | Requirement                                             |
| --------------------- | ------------------------------------------------------- |
| Soft delete awareness | `findAll()` filters deleted records                     |
| `softDelete()`        | Updates `deleted` fields instead of deleting            |
| `findById()`          | Filters out soft-deleted rows                           |
| Inheritance           | Must be `@NoRepositoryBean` and used via specialization |

### ✅ 3.3 `BaseService`

| Criteria       | Requirement                                          |
| -------------- | ---------------------------------------------------- |
| CRUD           | `findById`, `create`, `update`, `delete` implemented |
| Bulk ops       | `createAll`, `updateAll`, `deleteAll`                |
| Search         | `search(criteria, pageable)` method present          |
| Import/export  | `importData()`, `export()` support provided          |
| DTO conversion | Assumes a `Mapper<T, D>` to convert Entity <-> DTO   |

### ✅ 3.4 `PermissionEvaluator` + Annotation

| Criteria              | Requirement                                                |
| --------------------- | ---------------------------------------------------------- |
| Method protection     | Uses `@CheckPermission(entityType, action)`                |
| Evaluator logic       | Generates permission string and matches against user roles |
| Custom evaluator bean | Spring bean used via `@PreAuthorize` expressions           |

### ✅ 3.5 `Hooks`

| Criteria           | Requirement                                          |
| ------------------ | ---------------------------------------------------- |
| Lifecycle coverage | Supports all pre/post CRUD hooks                     |
| Registration       | Hooks can be registered per entity class in runtime  |
| Registry lookup    | Hooks resolved dynamically and applied per operation |

### ✅ 3.6 `BaseServiceImpl<T, D, ID>`

| Criteria        | Requirement                                        |
| --------------- | -------------------------------------------------- |
| Uses repository | Injects proper `BaseRepository<T, ID>`             |
| Uses mapper     | Injects proper `EntityMapper<T, D>`                |
| Applies hooks   | Runs pre/post hooks around lifecycle events        |
| Transactional   | Marked with `@Transactional`                       |
| Generic         | Works across all entities generically via generics |

---

## 🧪 4. Additional Tests for Windsurf

| Test Type              | What to Validate                                             |
| ---------------------- | ------------------------------------------------------------ |
| Code Generation Test   | Generates correct service, repo, and mapper using specs      |
| Hook Invocation Test   | Hook methods get invoked at correct lifecycle moments        |
| Security Enforcement   | Methods fail when permission is missing                      |
| Soft Delete Filtering  | `findAll()` and `findById()` exclude deleted records         |
| Audit Field Population | `createdAt`, `createdBy`, etc., are auto-filled              |
| Versioning             | Optimistic locking with version column works                 |
| Import/Export          | DTOs can be exported/imported correctly                      |
| Entity Isolation       | Generic base layer supports multiple domain entities cleanly |

---

## 📌 5. Optional Extensions for Windsurf Validation

* Enforce `@Mapper` usage for Entity ↔ DTO
* Use test containers for integration test generation
* Generate Spring REST Docs from controller specs
* Enforce caching annotations if marked in spec
* Validate soft delete SQL clause in `@SQLDelete`, `@Where`
