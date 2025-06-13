package com.example.recipehub_ui.repository;

import com.example.recipehub_ui.model.Rating;
import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRecipeId(Long recipeId);
    Optional<Rating> findByRecipeAndRater(Recipe recipe, User rater);
}
