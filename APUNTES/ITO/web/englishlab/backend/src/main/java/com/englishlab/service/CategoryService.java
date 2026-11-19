package com.englishlab.service;

import com.englishlab.dto.category.CategoryRequest;
import com.englishlab.dto.category.CategoryResponse;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.exception.BusinessRuleException;
import com.englishlab.exception.NotFoundException;
import com.englishlab.model.entity.Category;
import com.englishlab.repository.CategoryRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de categorías. Escritura ADMIN (seguridad en el controller); lectura ADMIN+TEACHER.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(String text, Pageable pageable) {
        Specification<Category> spec = (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like));
        };
        Page<Category> page = categoryRepository.findAll(spec, pageable);
        List<CategoryResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException("Ya existe una categoría con ese nombre");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(request.description());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessRuleException("Ya existe una categoría con ese nombre");
        }
        category.setName(name);
        category.setDescription(request.description());
        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}