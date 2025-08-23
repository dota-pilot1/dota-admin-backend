package com.company.dotaadminbackend.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authority {
    
    private Long id;
    private String name;
    private String description;
    private String category;
}