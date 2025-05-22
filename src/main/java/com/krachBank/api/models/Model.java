package com.krachBank.api.models;

import com.krachBank.api.dto.DTO;

import jakarta.persistence.Entity;

@Entity
public interface Model {
    public DTO toDTO();
}
