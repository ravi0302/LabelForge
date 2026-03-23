package com.labelforge.service;

import com.labelforge.dto.CategoryRequest;
import com.labelforge.dto.CategoryResponse;
import com.labelforge.exception.ResourceNotFoundException;
import com.labelforge.model.Category;
import com.labelforge.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
            .stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.getName())) {
            throw new IllegalArgumentException("Category already exists: " + req.getName());
        }
        Category c = new Category();
        c.setName(req.getName().trim());
        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        // products.category_id will be set to NULL (ON DELETE SET NULL in schema)
        categoryRepository.delete(c);
    }

    public Category getOrNull(Long id) {
        if (id == null) return null;
        return categoryRepository.findById(id).orElse(null);
    }

    public Category getOrCreateByName(String name) {
        if (name == null || name.isBlank()) return null;
        return categoryRepository.findByNameIgnoreCase(name.trim())
            .orElseGet(() -> {
                Category c = new Category();
                c.setName(name.trim());
                return categoryRepository.save(c);
            });
    }
}
