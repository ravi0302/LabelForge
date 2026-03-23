package com.labelforge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labelforge.dto.CreateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // rolls back after each test
class ProductControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static final String BASE = "/api/v1/products";

    @Test
    void createProduct_returnsCreatedWithEan() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Wheat Flour");
        req.setWeight("500g");
        req.setPrice(new BigDecimal("45.00"));

        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ean").isString())
            .andExpect(jsonPath("$.ean").value(org.hamcrest.Matchers.matchesPattern("\\d{13}")))
            .andExpect(jsonPath("$.name").value("Wheat Flour"));
    }

    @Test
    void createProduct_validationFailsOnBlankName() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("");
        req.setWeight("500g");
        req.setPrice(new BigDecimal("45.00"));

        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void listProducts_returnsEmptyArray() throws Exception {
        mockMvc.perform(get(BASE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_returns404ForMissingProduct() throws Exception {
        mockMvc.perform(get(BASE + "/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returns204() throws Exception {
        // Create first
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Test Product");
        req.setWeight("1kg");
        req.setPrice(new BigDecimal("99.00"));

        String response = mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Delete
        mockMvc.perform(delete(BASE + "/" + id))
            .andExpect(status().isNoContent());

        // Verify gone
        mockMvc.perform(get(BASE + "/" + id))
            .andExpect(status().isNotFound());
    }
}
