package com.example.recipehub_ui.service;

import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl service;

    private Recipe r1, r2;

    @BeforeEach
    void setUp() {
        r1 = new Recipe();
        r1.setId(1L);
        r1.setUploadDate(Instant.parse("2025-01-01T00:00:00Z"));

        r2 = new Recipe();
        r2.setId(2L);
        r2.setUploadDate(Instant.parse("2025-02-01T00:00:00Z"));
    }

    @Test
    void getRecentRecipes_shouldReturnRecipesSortedByDate() {
        when(recipeRepository.findAllByOrderByUploadDateDesc())
                .thenReturn(List.of(r2, r1));

        List<Recipe> recent = service.getRecentRecipes();

        assertThat(recent).containsExactly(r2, r1);
        verify(recipeRepository).findAllByOrderByUploadDateDesc();
    }

    @Test
    void getRecipeById_existingId_returnsRecipe() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(r1));

        Recipe found = service.getRecipeById(1L);

        assertThat(found).isSameAs(r1);
    }

    @Test
    void getRecipeById_missingId_throwsNotFound() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRecipeById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Recipe not found");
    }

    @Test
    void saveRecipe_savesAndReturns() {
        when(recipeRepository.save(r1)).thenReturn(r1);

        Recipe saved = service.saveRecipe(r1);

        assertThat(saved).isSameAs(r1);
        verify(recipeRepository).save(r1);
    }
}
