package com.krachbank.api.mappers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.models.Model;

public abstract class BaseMapper<M extends Model, Req extends DTO, Res extends DTO> {

    /**
     * Converts a {@link Page} of model entities to a {@link PaginatedResponseDTO}
     * of response DTOs.
     *
     * @param page the {@link Page} containing model entities to be converted
     * @return a {@link PaginatedResponseDTO} containing the converted response DTOs
     *         and pagination metadata
     * @throws Exception 
     */
    public PaginatedResponseDTO<Res> toPaginatedResponse(Page<M> page) throws Exception {

        PaginatedResponseDTO<Res> response = new PaginatedResponseDTO<Res>();
        response.setItems(toResponseList(page.getContent()));
        response.setTotalItems(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());

        return response;
    }

    /**
     * Converts a list of model objects to a list of response DTOs.
     *
     * @param models the list of model objects to convert
     * @return a list of response DTOs corresponding to the input models
     * @throws Exception
     */
    public List<Res> toResponseList(List<M> models) throws Exception {
        List<Res> dtos = new ArrayList<>();
        for (M model : models) {
            dtos.add(toResponse(model));
        }
        return dtos;
    }

    /**
     * Converts a list of DTOs to a list of model objects.
     *
     * @param dtoList the list of DTO objects to be converted
     * @return a list of model objects corresponding to the given DTOs
     * @throws Exception if an error occurs during the conversion of any DTO
     */
    public List<M> toModelList(List<Req> dtoList) throws Exception {
        List<M> accounts = new ArrayList<>();
        for (Req dto : dtoList) {
            accounts.add(toModel(dto));
        }
        return accounts;
    }

    /**
     * Converts the given request object to its corresponding model representation.
     *
     * @param model the request object to be converted
     * @return the model representation of the given request
     * @throws Exception if the conversion fails
     */
    abstract M toModel(Req model) throws Exception;


    /**
     * Converts the given model object to its corresponding response DTO.
     *
     * @param model the model object to be converted
     * @return the response DTO representation of the given model
     * @throws Exception if the conversion fails
     */
    abstract Res toResponse(M model) throws Exception;

}
