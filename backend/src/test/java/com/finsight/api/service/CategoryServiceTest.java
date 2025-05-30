package com.finsight.api.service;

import com.finsight.api.dto.CategoryDTO;
import com.finsight.api.model.Category;
import com.finsight.api.repository.CategoryRepository;
import com.finsight.api.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category sampleCategory;
    private CategoryDTO sampleCategoryDTO;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(1L);
        sampleCategory.setName("Food");
        sampleCategory.setDescription("Food expenses");

        sampleCategoryDTO = new CategoryDTO();
        sampleCategoryDTO.setId(1L);
        sampleCategoryDTO.setName("Food");
        sampleCategoryDTO.setDescription("Food expenses");
    }

    @Test
    void getAllCategories_ShouldReturnPagedResults() {
        // Given
        Page<Category> categoryPage = new PageImpl<>(List.of(sampleCategory));
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);

        // When
        Page<CategoryDTO> result = categoryService.getAllCategories(PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Food");
        verify(categoryRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        // When
        CategoryDTO result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getDescription()).isEqualTo("Food expenses");
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_WhenNotExists_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found: 999");
    }

    @Test
    void createCategory_ShouldReturnSavedCategory() {
        // Given
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        // When
        CategoryDTO result = categoryService.createCategory(sampleCategoryDTO);

        // Then
        assertThat(result.getName()).isEqualTo("Food");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenExists_ShouldReturnUpdatedCategory() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        // When
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Updated Food");
        updateDTO.setDescription("Updated description");
        
        CategoryDTO result = categoryService.updateCategory(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenNotExists_ShouldThrowException() {
        // Given
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        // When/Then
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Updated Food");
        
        assertThatThrownBy(() -> categoryService.updateCategory(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteCategory_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_WhenNotExists_ShouldThrowException() {
        // Given
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getCategoryByName_WhenExists_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(sampleCategory));

        // When
        CategoryDTO result = categoryService.getCategoryByName("Food");

        // Then
        assertThat(result.getName()).isEqualTo("Food");
        verify(categoryRepository).findByName("Food");
    }

    @Test
    void getCategoryByName_WhenNotExists_ShouldThrowException() {
        // Given
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryByName("NonExistent"))
                .isInstanceOf(EntityNotFoundException.class);
    }
} 