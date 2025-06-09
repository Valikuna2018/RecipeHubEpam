package com.example.recipehub_ui.controller;

import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.service.CategoryService;
import com.example.recipehub_ui.service.RecipeService;
import com.example.recipehub_ui.service.TagService;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RecipeService recipeService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private TagService tagService;

    @Test
    void home_showsIndexAndModel() throws Exception {
        Recipe r = new Recipe();
        r.setId(1L);
        r.setTitle("TDD Cake");
        r.setUploadDate(Instant.now());
        when(recipeService.getRecentRecipes()).thenReturn(List.of(r));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("TDD Cake")));
    }

    @Test
    void details_showsRecipeDetails() throws Exception {
        Recipe r = new Recipe();
        r.setId(42L);
        r.setTitle("MockMvc Stew");
        when(recipeService.getRecipeById(42L)).thenReturn(r);

        mvc.perform(get("/recipes/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-details"))
                .andExpect(model().attribute("recipe", r))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MockMvc Stew")));
    }

    @Test
    void search_withQuery_returnsSearchView() throws Exception {
        when(recipeService.searchRecipes("egg")).thenReturn(List.of());
        mvc.perform(get("/search").param("q", "egg"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("results"))
                .andExpect(model().attribute("q", "egg"));
    }
}
