package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.domain.model.Player;
import com.company.dotaadminbackend.application.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Player> create(@RequestParam String name) {
        Player p = service.create(name);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from backend");
    }
}
