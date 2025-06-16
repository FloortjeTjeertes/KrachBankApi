package com.krachbank.api.controllers;



import com.krachbank.api.dto.DTO;
import com.krachbank.api.models.Model;

public interface Controller<M extends Model ,  Res extends DTO,Req extends DTO > {

    
    M toModel(Req model);

    Res toResponse(M model);



} 
