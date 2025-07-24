package com.lending.backend.controller;

import com.lending.backend.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/errors")
public class TestExceptionController {

    @GetMapping("/not-found")
    public ResponseEntity<?> triggerNotFound() {
        throw new ResourceNotFoundException("TestResource", "id", "123");
    }

    @GetMapping("/validation")
    public ResponseEntity<?> triggerValidation(
            @RequestParam @NotBlank(message = "Name is required") String name,
            @RequestParam @Email(message = "Email should be valid") String email) {
        return ResponseEntity.ok("Validation passed");
    }

    @GetMapping("/unauthorized")
    public ResponseEntity<?> triggerUnauthorized() {
        // This will be secured and require authentication
        return ResponseEntity.ok("This should be unreachable without authentication");
    }

    @GetMapping("/forbidden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> triggerForbidden() {
        return ResponseEntity.ok("This should be unreachable without ADMIN role");
    }

    @GetMapping("/server-error")
    public ResponseEntity<?> triggerServerError() {
        // Simulate a server error
        throw new RuntimeException("This is a test server error");
    }

    @PostMapping("/validation/body")
    public ResponseEntity<?> validateBody(@Valid @RequestBody TestRequest request) {
        return ResponseEntity.ok("Request body is valid");
    }

    public static class TestRequest {
        @NotBlank(message = "Name is required")
        private String name;
        
        @Email(message = "Email should be valid")
        private String email;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
