package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.domain.model.User;
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
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String role) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users;
        
        if (role != null && !role.trim().isEmpty()) {
            users = userService.findUsersByRole(role.toUpperCase(), pageable);
        } else {
            users = userService.findAllUsers(pageable);
        }
        
        Long adminCount = userService.countUsersByRole("ADMIN");
        Long userCount = userService.countUsersByRole("USER");
        
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
    public ResponseEntity<?> getAllUsersWithoutPaging(
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
        Page<User> users;
        
        if (role != null && !role.trim().isEmpty()) {
            users = userService.findUsersByRole(role.toUpperCase(), pageable);
        } else {
            users = userService.findAllUsers(pageable);
        }
        
        Long adminCount = userService.countUsersByRole("ADMIN");
        Long userCount = userService.countUsersByRole("USER");
        
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
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(userOpt.get());
    }

    @GetMapping("/search/email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(userOpt.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            User updatedUser = userService.updateUser(id, request.username(), request.email());
            return ResponseEntity.ok(Map.of(
                "message", "User updated successfully",
                "user", updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = userService.updateCurrentUserProfile(request.username(), request.email());
            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteCurrentUserProfile(@RequestBody DeleteAccountRequest request) {
        try {
            userService.deleteCurrentUser(request.password());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public record UpdateUserRequest(String username, String email) {}
    public record UpdateProfileRequest(String username, String email) {}
    public record DeleteAccountRequest(String password) {}
}