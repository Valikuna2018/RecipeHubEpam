package com.example.recipehub_ui.controller;

import com.example.recipehub_ui.model.Category;
import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.model.Tag;
import com.example.recipehub_ui.service.CategoryService;
import com.example.recipehub_ui.service.RecipeService;
import com.example.recipehub_ui.service.TagService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final RecipeService   recipeService;
    private final CategoryService categoryService;
    private final TagService      tagService;

    public HomeController(RecipeService recipeService,
                          CategoryService categoryService,
                          TagService tagService) {
        this.recipeService   = recipeService;
        this.categoryService = categoryService;
        this.tagService      = tagService;
    }

    /** Prevent Spring from trying to bind raw 'tags' & 'imageFile' into Recipe */
    @InitBinder("recipe")
    public void initRecipeBinder(WebDataBinder binder) {
        binder.setDisallowedFields("tags", "imagePath");
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recipes", recipeService.getRecentRecipes());
        return "index";
    }

    @GetMapping("/recipes/top-rated")
    public String topRated(Model model) {
        model.addAttribute("recipes", recipeService.getTopRatedRecipes());
        return "top-rated";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value="q", required=false) String keyword,
                         Model model) {
        List<Recipe> results = (keyword == null || keyword.isBlank())
                ? List.of()
                : recipeService.searchRecipes(keyword);
        model.addAttribute("results", results);
        model.addAttribute("q", keyword);
        return "search";
    }

    @GetMapping("/recipes/upload")
    public String uploadForm(Model model) {
        model.addAttribute("recipe",     new Recipe());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("tags",       tagService.getAllTags());
        return "upload";
    }

    @GetMapping("/recipes/{id}")
    public String details(@PathVariable Long id, Model model) {
        Recipe recipe = recipeService.getRecipeById(id);
        model.addAttribute("recipe", recipe);
        return "recipe-details";
    }

    @PostMapping("/recipes/{id}/rate")
    public String rateRecipe(@PathVariable Long id,
                             @RequestParam int stars) {
        recipeService.rateRecipe(id, stars);
        return "redirect:/recipes/" + id;
    }


    @PostMapping("/recipes/upload")
    public String uploadSubmit(
            @ModelAttribute Recipe         recipe,
            @RequestParam("tags")         List<String>   tagNames,
            @RequestParam("imageFile")    MultipartFile  imageFile
    ) throws IOException {
        // 1) Save the uploaded file to src/main/resources/static/images
        Path imagesDir = Paths.get(
                System.getProperty("user.dir"),
                "src","main","resources","static","images"
        );
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String original = imageFile.getOriginalFilename();
            String filename = UUID.randomUUID() + "_" + original;
            try (InputStream in = imageFile.getInputStream()) {
                Files.copy(in,
                        imagesDir.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            recipe.setImagePath(filename);
        }

        // 2) Lookup Category & Tags by name
        Category cat = categoryService.findByName(recipe.getCategory().getName());
        recipe.setCategory(cat);
        Set<Tag> tagSet = tagNames.stream()
                .map(tagService::findByName)
                .collect(Collectors.toSet());
        recipe.setTags(tagSet);

        // 3) Persist and redirect home
        recipeService.saveRecipe(recipe);
        return "redirect:/";
    }
}
