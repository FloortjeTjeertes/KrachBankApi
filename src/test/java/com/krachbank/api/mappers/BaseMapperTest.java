package com.krachbank.api.mappers;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.models.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BaseMapperTest {

    // Dummy Model, Req DTO, and Res DTO for testing
    static class DummyModel implements Model {
        private Long id;
        private String value;

        public DummyModel(Long id, String value) {
            this.id = id;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    static class DummyReqDTO implements DTO {
        private Long id;
        private String value;

        public DummyReqDTO(Long id, String value) {
            this.id = id;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    static class DummyResDTO implements DTO {
        private Long id;
        private String value;

        public DummyResDTO(Long id, String value) {
            this.id = id;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    // Concrete implementation for testing
    static class DummyMapper extends BaseMapper<DummyModel, DummyReqDTO, DummyResDTO> {
        @Override
        DummyModel toModel(DummyReqDTO dto) {
            return new DummyModel(dto.getId(), dto.getValue());
        }

        @Override
        DummyResDTO toResponse(DummyModel model) {
            return new DummyResDTO(model.getId(), model.getValue());
        }
    }

    private DummyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DummyMapper();
    }

    @Test
    void testToResponseList() throws Exception {
        List<DummyModel> models = Arrays.asList(
                new DummyModel(1L, "A"),
                new DummyModel(2L, "B"));
        List<DummyResDTO> dtos = mapper.toResponseList(models);

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals("A", dtos.get(0).getValue());
        assertEquals(2L, dtos.get(1).getId());
        assertEquals("B", dtos.get(1).getValue());
    }

    @Test
    void testToResponseListEmpty() throws Exception {
        List<DummyResDTO> dtos = mapper.toResponseList(Collections.emptyList());
        assertTrue(dtos.isEmpty());
    }

    @Test
    void testToModelList() throws Exception {
        List<DummyReqDTO> reqs = Arrays.asList(
                new DummyReqDTO(10L, "X"),
                new DummyReqDTO(20L, "Y"));
        List<DummyModel> models = mapper.toModelList(reqs);

        assertEquals(2, models.size());
        assertEquals(10L, models.get(0).getId());
        assertEquals("X", models.get(0).getValue());
        assertEquals(20L, models.get(1).getId());
        assertEquals("Y", models.get(1).getValue());
    }

    @Test
    void testToModelListEmpty() throws Exception {
        List<DummyModel> models = mapper.toModelList(Collections.emptyList());
        assertTrue(models.isEmpty());
    }

    @Test
    void testToPaginatedResponse() throws Exception {
        List<DummyModel> models = Arrays.asList(
                new DummyModel(1L, "A"),
                new DummyModel(2L, "B"));
        Page<DummyModel> page = new PageImpl<>(models, PageRequest.of(1, 2), 5);

        PaginatedResponseDTO<DummyResDTO> response = mapper.toPaginatedResponse(page);

        assertEquals(2, response.getItems().size());
        assertEquals(5, response.getTotalItems());
        assertEquals(3, response.getTotalPages());
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getPageSize());
        assertEquals(1L, response.getItems().get(0).getId());
        assertEquals("A", response.getItems().get(0).getValue());
    }

    @Test
    void testToPaginatedResponseEmpty() throws Exception {
        Page<DummyModel> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        PaginatedResponseDTO<DummyResDTO> response = mapper.toPaginatedResponse(page);

        assertNotNull(response.getItems());
        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalItems());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
    }
}