package com.krachbank.api.models;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public interface Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id = 0L; 

    public DTO toDTO();
}
