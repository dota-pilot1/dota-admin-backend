package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.domain.model.User;
import com.company.dotaadminbackend.application.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userService.findAllUsers(pageable);
        
        return ResponseEntity.ok(Map.of(
            "users", users.getContent(),
            "totalElements", users.getTotalElements(),
            "totalPages", users.getTotalPages(),
            "currentPage", users.getNumber(),
            "size", users.getSize()
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsersWithoutPaging(
            @RequestParam(defaultValue = "500") int limit,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // 안전을 위해 최대 30000명으로 제한
        if (limit > 30000) {
            limit = 30000;
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(0, limit, sort);
        Page<User> users = userService.findAllUsers(pageable);
        
        return ResponseEntity.ok(Map.of(
            "users", users.getContent(),
            "totalElements", users.getTotalElements(),
            "returnedCount", users.getContent().size(),
            "isComplete", users.getContent().size() < limit
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(userOpt.get());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    public record UpdateUserRequest(String username, String email) {}
    public record UpdateProfileRequest(String username, String email) {}
}