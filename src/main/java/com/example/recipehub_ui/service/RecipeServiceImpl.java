package com.example.recipehub_ui.service;

import com.example.recipehub_ui.model.Rating;
import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.repository.RatingRepository;
import com.example.recipehub_ui.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RatingRepository ratingRepository;    // ← injected

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository,
                             RatingRepository ratingRepository) {
        this.recipeRepository = recipeRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public List<Recipe> getRecentRecipes() {
        return recipeRepository.findAllByOrderByUploadDateDesc();
    }

    @Override
    public List<Recipe> getTopRatedRecipes() {
        // no persistent avg field ⇒ sort in Java
        return recipeRepository.findAll().stream()
                .sorted(Comparator.comparingDouble(Recipe::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> searchRecipes(String keyword) {
        return recipeRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    @Override
    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    @Override
    public void rateRecipe(Long recipeId, int stars) {
        Recipe recipe = getRecipeById(recipeId);
        Rating r = new Rating();
        r.setRecipe(recipe);
        r.setStars(stars);
        ratingRepository.save(r);
        // no need to persist avg on Recipe—getAverageRating() is @Transient
    }
}
