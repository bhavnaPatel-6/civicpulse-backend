package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.CategoryCreateRequest;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Admin only
    @PostMapping("/admin/categories")
    public ResponseEntity<Category> createCategory(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        return ResponseEntity.ok(
                categoryService.createCategory(request)
        );
    }

    // Citizen/Authority - active categories
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(
                categoryService.getAllActiveCategories()
        );
    }
}