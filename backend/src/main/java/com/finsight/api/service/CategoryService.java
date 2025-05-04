package com.finsight.api.service;

import com.finsight.api.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<CategoryDTO> getAllCategories(Pageable pageable);
    CategoryDTO getCategoryById(Long id);
    CategoryDTO getCategoryByName(String name);
    CategoryDTO createCategory(CategoryDTO dto);
    CategoryDTO updateCategory(Long id, CategoryDTO dto);
    void deleteCategory(Long id);
}
