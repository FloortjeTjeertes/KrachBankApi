package com.krachbank.api.controllers;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.models.Model;

public interface Controller<M extends Model , D extends DTO> {

    
    M toModel(D dto);
} 
