package com.example.recipehub_ui.service;

import com.example.recipehub_ui.model.Rating;
import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.model.User;
import com.example.recipehub_ui.repository.RatingRepository;
import com.example.recipehub_ui.repository.RecipeRepository;
import com.example.recipehub_ui.repository.UserRepository;
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
    private final RatingRepository ratingRepository;
    private final UserRepository   userRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository rr,
                             RatingRepository rtr,
                             UserRepository ur) {
        this.recipeRepository = rr;
        this.ratingRepository = rtr;
        this.userRepository   = ur;
    }

    @Override
    public List<Recipe> getRecentRecipes() {
        return recipeRepository.findAllByOrderByUploadDateDesc();
    }

    @Override
    public List<Recipe> getTopRatedRecipes() {
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
    public void rateRecipe(Long recipeId, int stars, String username) {
        Recipe recipe = getRecipeById(recipeId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        ratingRepository.findByRecipeAndRater(recipe, user)
                .ifPresent(ratingRepository::delete);

        Rating r = new Rating();
        r.setRecipe(recipe);
        r.setRater(user);
        r.setStars(stars);
        ratingRepository.save(r);
    }

    @Override
    public List<Recipe> getRecipesByOwner(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
        return recipeRepository.findAllByOwner(owner);
    }

    @Override
    public void deleteRecipe(Long id, String username) {
        Recipe recipe = getRecipeById(id);
        if (!recipe.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own recipes");
        }
        recipeRepository.delete(recipe);
    }

    @Override
    public boolean isDuplicate(Recipe candidate) {
        User owner = candidate.getOwner();
        List<Recipe> theirs = recipeRepository.findAllByOwnerOrderByUploadDateDesc(owner);

        for (Recipe r : theirs) {
            if (r.getTitle().equals(candidate.getTitle())
                    && r.getIngredients().equals(candidate.getIngredients())
                    && r.getInstructions().equals(candidate.getInstructions())
                    && r.getPrepTimeMinutes().equals(candidate.getPrepTimeMinutes())
                    && r.getCookTimeMinutes().equals(candidate.getCookTimeMinutes())
                    && r.getCategory().getName().equals(candidate.getCategory().getName())
                    && r.getTags().stream()
                    .map(t->t.getName())
                    .collect(Collectors.toSet())
                    .equals(candidate.getTags().stream()
                            .map(t->t.getName())
                            .collect(Collectors.toSet()))
            ) {
                return true;
            }
        }
        return false;
    }
}
