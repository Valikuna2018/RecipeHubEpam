package com.example.recipehub_ui.repository;

import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByUploadDateDesc();

    List<Recipe> findByTitleContainingIgnoreCase(String keyword);

    List<Recipe> findAllByOwner(User owner);
    List<Recipe> findAllByOwnerOrderByUploadDateDesc(User owner);


}
