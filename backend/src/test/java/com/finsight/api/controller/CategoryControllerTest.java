package com.finsight.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.api.dto.CategoryDTO;
import com.finsight.api.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryDTO sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new CategoryDTO();
        sampleCategory.setId(1L);
        sampleCategory.setName("Food");
        sampleCategory.setDescription("Food expenses");
    }

    @Test
    void getAllCategories_ShouldReturnPagedResults() throws Exception {
        Page<CategoryDTO> page = new PageImpl<>(List.of(sampleCategory));
        when(categoryService.getAllCategories(any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/categories")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Food"));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryById(anyLong()))
                .thenReturn(sampleCategory);

        mockMvc.perform(get("/api/categories/1")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void createCategory_ShouldReturnCreatedCategory() throws Exception {
        when(categoryService.createCategory(any(CategoryDTO.class)))
                .thenReturn(sampleCategory);

        mockMvc.perform(post("/api/categories")
                        .with(jwt().authorities(() -> "fin:app"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        when(categoryService.updateCategory(anyLong(), any(CategoryDTO.class)))
                .thenReturn(sampleCategory);

        mockMvc.perform(put("/api/categories/1")
                        .with(jwt().authorities(() -> "fin:app"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(anyLong());

        mockMvc.perform(delete("/api/categories/1")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCategoryByName_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryByName(anyString()))
                .thenReturn(sampleCategory);

        mockMvc.perform(get("/api/categories/name/Food")
                        .with(jwt().authorities(() -> "fin:app")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void accessWithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
} 