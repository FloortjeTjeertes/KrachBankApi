package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.DTO;

import com.krachbank.api.models.Model;
public interface Service<Res extends DTO, M extends Model> {

    /**
     * @deprecated use controller to convert to DTO instead
     */
    @Deprecated
    Res toDTO(M model);

    /**
     * @deprecated use controller to convert to DTO instead
     */
    @Deprecated
    List<Res> toDTO(List<M> fields);



}
