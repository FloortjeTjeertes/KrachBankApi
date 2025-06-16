package com.krachbank.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class PaginatedResponseDTO <D extends DTO> {

    List<D> items;
    long totalItems;
    int totalPages;
    int currentPage;
    int pageSize;

   



}
