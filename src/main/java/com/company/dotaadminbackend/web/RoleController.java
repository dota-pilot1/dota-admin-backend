package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.RoleService;
import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import com.company.dotaadminbackend.infrastructure.dto.CreateRoleRequest;
import com.company.dotaadminbackend.infrastructure.dto.RoleResponse;
import com.company.dotaadminbackend.infrastructure.dto.UpdateRoleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.debug("[RoleController] Fetching all roles");
        long started = System.currentTimeMillis();
        List<RoleEntity> roles = roleService.getAllRoles();
        List<RoleResponse> responses = roles.stream()
            .map(RoleResponse::from)
            .collect(Collectors.toList());
        log.debug("[RoleController] Retrieved {} roles in {} ms", responses.size(), System.currentTimeMillis()-started);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        try {
            RoleEntity role = roleService.getRoleById(id);
            log.debug("[RoleController] Found role id={} name={}", role.getId(), role.getName());
            return ResponseEntity.ok(RoleResponse.from(role));
        } catch (RuntimeException e) {
            log.warn("[RoleController] Role not found id={} - {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        try {
            RoleEntity role = roleService.createRole(request);
            log.info("[RoleController] Created role id={} name={}", role.getId(), role.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(role));
        } catch (RuntimeException e) {
            log.warn("[RoleController] Failed to create role name={} - {}", request.getName(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateRoleRequest request) {
        try {
            RoleEntity role = roleService.updateRole(id, request);
            log.info("[RoleController] Updated role id={} newName={}", id, request.getName());
            return ResponseEntity.ok(RoleResponse.from(role));
        } catch (RuntimeException e) {
            log.warn("[RoleController] Failed to update role id={} - {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            log.info("[RoleController] Deleted role id={}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("[RoleController] Failed to delete role id={} - {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}