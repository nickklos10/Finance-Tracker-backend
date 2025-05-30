package com.finsight.api.service.impl;

import com.finsight.api.dto.CategoryDTO;
import com.finsight.api.model.Category;
import com.finsight.api.repository.CategoryRepository;
import com.finsight.api.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "categories")
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Override
    @Cacheable(key = "'all-page-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepo.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @Cacheable(key = "'id-' + #id")
    public CategoryDTO getCategoryById(Long id) {
        Category cat = categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        return toDto(cat);
    }

    @Override
    @Cacheable(key = "'name-' + #name")
    public CategoryDTO getCategoryByName(String name) {
        return categoryRepo.findByName(name)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + name));
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category '" + dto.getName() + "' already exists");
        }
        CategoryDTO result = toDto(categoryRepo.save(toEntity(dto)));
        
        // Evict pagination caches since a new category affects page results
        evictAllPaginationCaches();
        log.debug("Evicted pagination caches after creating category: {}", result.getName());
        
        return result;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category existingCategory = categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        
        String oldName = existingCategory.getName();
        
        dto.setId(id);
        CategoryDTO result = toDto(categoryRepo.save(toEntity(dto)));
        
        // Evict specific cache entries using proper key patterns
        evictCachesByKey("id-" + id);
        if (oldName != null) {
            evictCachesByKey("name-" + oldName);
        }
        if (result.getName() != null && !result.getName().equals(oldName)) {
            evictCachesByKey("name-" + result.getName());
        }
        
        // Evict pagination caches since category name might have changed order
        evictAllPaginationCaches();
        
        log.debug("Evicted cache entries for category update: old={}, new={}", oldName, result.getName());
        
        return result;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        
        String categoryName = category.getName();
        
        categoryRepo.deleteById(id);
        
        // Evict specific cache entries
        evictCachesByKey("id-" + id);
        if (categoryName != null) {
            evictCachesByKey("name-" + categoryName);
        }
        
        // Evict pagination caches since deletion affects page results
        evictAllPaginationCaches();
        
        log.debug("Evicted cache entries for deleted category: id={}, name={}", id, categoryName);
    }

    /**
     * Evicts cache entries by specific key
     */
    private void evictCachesByKey(String key) {
        if (cacheManager == null) {
            log.debug("CacheManager not available, skipping cache eviction for key: {}", key);
            return;
        }
        
        try {
            var cache = cacheManager.getCache("categories");
            if (cache != null) {
                cache.evictIfPresent(key);
                log.debug("Evicted cache entry with key: {}", key);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache entry with key: {}", key, e);
        }
    }

    /**
     * Evicts all pagination-related cache entries
     * For scalability, this clears all pagination entries since we can't easily
     * pattern-match cache keys in simple cache implementations
     */
    private void evictAllPaginationCaches() {
        if (cacheManager == null) {
            log.debug("CacheManager not available, skipping pagination cache eviction");
            return;
        }
        
        try {
            var cache = cacheManager.getCache("categories");
            if (cache != null) {
                // In a more sophisticated cache implementation (like Redis),
                // you could use pattern-based deletion
                cache.clear();
                log.debug("Cleared all category caches including pagination");
            }
        } catch (Exception e) {
            log.warn("Failed to evict pagination caches", e);
        }
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
        return c;
    }
}
