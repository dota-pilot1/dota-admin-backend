package com.company.dotaadminbackend.infrastructure.adapter;

import com.company.dotaadminbackend.infrastructure.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPlayerRepository extends JpaRepository<PlayerEntity, Long> {
}
