
package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.infrastructure.entity.PlayerEntity;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataPlayerRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final SpringDataPlayerRepository repository;

    public PlayerService(SpringDataPlayerRepository repository) {
        this.repository = repository;
    }

    public PlayerEntity create(String name) {
        PlayerEntity entity = new PlayerEntity();
        entity.setName(name);
        return repository.save(entity);
    }
}
