package com.company.dotaadminbackend.infrastructure.adapter;

import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    Page<UserEntity> findByRole(RoleEntity role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.role.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);
}