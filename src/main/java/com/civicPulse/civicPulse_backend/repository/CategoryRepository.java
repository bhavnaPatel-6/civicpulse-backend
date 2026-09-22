package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.Department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByDefaultDepartment(Department department);

    boolean existsByName(String name);
}