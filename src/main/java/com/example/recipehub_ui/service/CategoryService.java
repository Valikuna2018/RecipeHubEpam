package com.example.recipehub_ui.service;

import com.example.recipehub_ui.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category findByName(String name);
}
