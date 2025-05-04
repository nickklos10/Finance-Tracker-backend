package com.finsight.api.service.impl;

import com.finsight.api.dto.CategoryDTO;
import com.finsight.api.model.Category;
import com.finsight.api.repository.CategoryRepository;
import com.finsight.api.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor                                    // Lombok ctor :contentReference[oaicite:3]{index=3}
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;

    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepo.findAll(pageable)               // built-in paging :contentReference[oaicite:4]{index=4}
                .map(this::toDto);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category cat = categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        return toDto(cat);
    }

    @Override
    public CategoryDTO getCategoryByName(String name) {
        return categoryRepo.findByName(name)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + name));
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepo.existsByName(dto.getName())) {     // cheap exists-check :contentReference[oaicite:5]{index=5}
            throw new IllegalArgumentException("Category '" + dto.getName() + "' already exists");
        }
        return toDto(categoryRepo.save(toEntity(dto)));
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        dto.setId(id);
        return toDto(categoryRepo.save(toEntity(dto)));
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        categoryRepo.deleteById(id);
    }

    /* ---------- mapping helpers ---------- */

    private CategoryDTO toDto(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription());
    }

    private Category toEntity(CategoryDTO d) {
        Category c = new Category();
        c.setId(d.getId());
        c.setName(d.getName());
        c.setDescription(d.getDescription());
        // leave transactions null or initialize as empty list if you prefer
        return c;
    }
}
