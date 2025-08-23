package com.company.dotaadminbackend.infrastructure.adapter;

import com.company.dotaadminbackend.infrastructure.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long> {
    
    Optional<AuthorityEntity> findByName(String name);
    
    List<AuthorityEntity> findByCategory(String category);
    
    @Query("SELECT a FROM AuthorityEntity a " +
           "JOIN RoleAuthorityEntity ra ON a.id = ra.authorityId " +
           "WHERE ra.roleId = :roleId")
    List<AuthorityEntity> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT a FROM AuthorityEntity a " +
           "JOIN UserAuthorityEntity ua ON a.id = ua.authorityId " +
           "WHERE ua.userId = :userId")
    List<AuthorityEntity> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT a FROM AuthorityEntity a " +
           "LEFT JOIN RoleAuthorityEntity ra ON a.id = ra.authorityId " +
           "LEFT JOIN UserAuthorityEntity ua ON a.id = ua.authorityId " +
           "LEFT JOIN UserEntity u ON u.role.id = ra.roleId OR u.id = ua.userId " +
           "WHERE u.id = :userId")
    List<AuthorityEntity> findAllUserAuthorities(@Param("userId") Long userId);
}