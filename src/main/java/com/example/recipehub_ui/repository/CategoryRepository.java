package com.example.recipehub_ui.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.recipehub_ui.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}
