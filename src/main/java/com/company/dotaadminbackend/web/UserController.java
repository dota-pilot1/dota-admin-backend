package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.application.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUserEntitys(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String role) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserEntity> users;
        
        if (role != null && !role.trim().isEmpty()) {
            users = userService.findUserEntitysByRole(role.toUpperCase(), pageable);
        } else {
            users = userService.findAllUserEntitys(pageable);
        }
        
        Long adminCount = userService.countUserEntitysByRole("ADMIN");
        Long userCount = userService.countUserEntitysByRole("USER");
        
        return ResponseEntity.ok(Map.of(
            "users", users.getContent(),
            "totalElements", users.getTotalElements(),
            "totalPages", users.getTotalPages(),
            "currentPage", users.getNumber(),
            "size", users.getSize(),
            "roleCounts", Map.of(
                "ADMIN", adminCount,
                "USER", userCount,
                "TOTAL", adminCount + userCount
            )
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUserEntitysWithoutPaging(
            @RequestParam(defaultValue = "20000") int limit,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String role) {
        
        // 안전을 위해 최대 30000명으로 제한
        if (limit > 30000) {
            limit = 30000;
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(0, limit, sort);
        Page<UserEntity> users;
        
        if (role != null && !role.trim().isEmpty()) {
            users = userService.findUserEntitysByRole(role.toUpperCase(), pageable);
        } else {
            users = userService.findAllUserEntitys(pageable);
        }
        
        Long adminCount = userService.countUserEntitysByRole("ADMIN");
        Long userCount = userService.countUserEntitysByRole("USER");
        
        return ResponseEntity.ok(Map.of(
            "users", users.getContent(),
            "totalElements", users.getTotalElements(),
            "returnedCount", users.getContent().size(),
            "isComplete", users.getContent().size() < limit,
            "roleCounts", Map.of(
                "ADMIN", adminCount,
                "USER", userCount,
                "TOTAL", adminCount + userCount
            )
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserEntityById(@PathVariable Long id) {
        Optional<UserEntity> userOpt = userService.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(userOpt.get());
    }

    @GetMapping("/search/email")
    public ResponseEntity<?> getUserEntityByEmail(@RequestParam String email) {
        Optional<UserEntity> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(userOpt.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserEntity(@PathVariable Long id, @RequestBody UpdateUserEntityRequest request) {
        try {
            UserEntity updatedUserEntity = userService.updateUserEntity(id, request.username(), request.email());
            return ResponseEntity.ok(Map.of(
                "message", "UserEntity updated successfully",
                "user", updatedUserEntity
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserEntity(@PathVariable Long id) {
        try {
            userService.deleteUserEntity(id);
            return ResponseEntity.ok(Map.of("message", "UserEntity deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserEntityProfile() {
        UserEntity currentUserEntity = userService.getCurrentUserEntity();
        return ResponseEntity.ok(currentUserEntity);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserEntityProfile(@RequestBody UpdateProfileRequest request) {
        try {
            UserEntity updatedUserEntity = userService.updateCurrentUserEntityProfile(request.username(), request.email());
            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", updatedUserEntity
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteCurrentUserEntityProfile(@RequestBody DeleteAccountRequest request) {
        try {
            userService.deleteCurrentUserEntity(request.password());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public record UpdateUserEntityRequest(String username, String email) {}
    public record UpdateProfileRequest(String username, String email) {}
    public record DeleteAccountRequest(String password) {}
}