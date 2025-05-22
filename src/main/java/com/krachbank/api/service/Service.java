package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.DTO;

import com.krachbank.api.models.Model;
public interface Service<D extends DTO, M extends Model> {

    D toDTO(M model);

    List<D> toDTO(List<M> fields);

    M toModel(D dto);


}
