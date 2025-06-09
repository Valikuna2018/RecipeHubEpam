package com.example.recipehub_ui.repository;

import com.example.recipehub_ui.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 1) Recent recipes — you already have this
    List<Recipe> findAllByOrderByUploadDateDesc();

    // 2) Top-rated recipes — custom JPQL to average the ratings.stars
    @Query("""
      SELECT r
      FROM Recipe r
      LEFT JOIN r.ratings rt
      GROUP BY r
      ORDER BY COALESCE(AVG(rt.stars),0) DESC
      """)
    List<Recipe> findAllByOrderByAverageRatingDesc();

    // 3) Simple case-insensitive title search
    List<Recipe> findByTitleContainingIgnoreCase(String keyword);
}
