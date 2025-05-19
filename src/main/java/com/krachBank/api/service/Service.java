package com.krachbank.api.service;

import com.krachbank.api.dto.DTO;

import com.krachbank.api.models.Model;
public interface Service<D extends DTO, M extends Model> {

    D toDTO(M model);

    M toModel(D dto);


}
