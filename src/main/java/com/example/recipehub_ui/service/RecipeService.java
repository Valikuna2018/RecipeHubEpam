package com.example.recipehub_ui.service;

import com.example.recipehub_ui.model.Recipe;
import java.util.List;

public interface RecipeService {
    List<Recipe> getRecentRecipes();
    List<Recipe> getTopRatedRecipes();
    List<Recipe> searchRecipes(String keyword);
    Recipe saveRecipe(Recipe recipe);

    Recipe getRecipeById(Long id);

    void rateRecipe(Long recipeId, int stars);

}
