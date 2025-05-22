package com.krachbank.api.models;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;

@Entity
public interface Model {
    public DTO toDTO();
}
