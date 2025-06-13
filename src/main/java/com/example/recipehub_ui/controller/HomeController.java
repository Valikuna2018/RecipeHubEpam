package com.example.recipehub_ui.controller;

import com.example.recipehub_ui.model.Category;
import com.example.recipehub_ui.model.Recipe;
import com.example.recipehub_ui.model.Tag;
import com.example.recipehub_ui.model.User;
import com.example.recipehub_ui.repository.UserRepository;
import com.example.recipehub_ui.service.CategoryService;
import com.example.recipehub_ui.service.RecipeService;
import com.example.recipehub_ui.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final UserRepository  userRepo;

    public HomeController(RecipeService recipeService,
                          CategoryService categoryService,
                          TagService tagService,
                          UserRepository userRepo) {
        this.recipeService   = recipeService;
        this.categoryService = categoryService;
        this.tagService      = tagService;
        this.userRepo        = userRepo;
    }

    /** Prevent binding of imagePath and tags directly */
    @InitBinder("recipe")
    public void initBinder(WebDataBinder b) {
        b.setDisallowedFields("tags", "imagePath");
    }

    @GetMapping("/")
    public String home(Model m) {
        log.debug("Loading home page with recent recipes");
        m.addAttribute("recipes", recipeService.getRecentRecipes());
        return "index";
    }

    @GetMapping("/recipes/top-rated")
    public String topRated(Model m) {
        log.debug("Loading top-rated page");
        m.addAttribute("recipes", recipeService.getTopRatedRecipes());
        return "top-rated";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required=false) String q, Model m) {
        log.debug("Search invoked for query='{}'", q);
        List<Recipe> results = (q == null || q.isBlank())
                ? List.of()
                : recipeService.searchRecipes(q);
        m.addAttribute("results", results);
        m.addAttribute("q", q);
        return "search";
    }

    @GetMapping("/recipes/upload")
    public String uploadForm(Model m, Principal principal) {
        if (principal == null) {
            log.error("Anonymous user tried to access upload form → redirect to login");
            return "redirect:/login";
        }
        log.debug("Showing upload form to '{}'", principal.getName());
        m.addAttribute("recipe", new Recipe());
        m.addAttribute("categories", categoryService.getAllCategories());
        m.addAttribute("tags",       tagService.getAllTags());
        return "upload";
    }

    @PostMapping("/recipes/upload")
    public String doUpload(
            @ModelAttribute Recipe        recipe,
            BindingResult                br,
            @RequestParam(value="tags", required=false) List<String> tagNames,
            @RequestParam("imageFile")   MultipartFile file,
            Principal                    principal,
            Model                        model
    ) throws IOException {
        if (principal == null) {
            log.error("Anonymous tried POST /recipes/upload → redirect to login ");
            return "redirect:/login";
        }

        // Prepare form data again on error
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("tags",       tagService.getAllTags());

        if (br.hasErrors()) {
            log.warn("Validation errors in upload form: {}", br.getAllErrors());
            return "upload";
        }

        User user = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("Logged-in user '{}' not found in DB", principal.getName());
                    return new IllegalStateException("User not found");
                });
        recipe.setOwner(user);
        recipe.setUsername(user.getUsername());
        log.info("User '{}' is uploading recipe '{}'", user.getUsername(), recipe.getTitle());

        Category cat = categoryService.findByName(recipe.getCategory().getName());
        recipe.setCategory(cat);

        Set<Tag> ts = (tagNames == null
                ? Collections.emptySet()
                : tagNames.stream().map(tagService::findByName).collect(Collectors.toSet()));
        recipe.setTags(ts);

        // Duplicate check
        if (recipeService.isDuplicate(recipe)) {
            log.warn("Duplicate recipe detected for user '{}': {}", user.getUsername(), recipe.getTitle());
            model.addAttribute("error",
                    "You’ve already uploaded an identical recipe—please change something and try again.");
            return "upload";
        }

        // Save image file
        Path uploadDir = Paths.get(System.getProperty("user.home"), "recipehub-uploads");
        Files.createDirectories(uploadDir);
        if (!file.isEmpty()) {
            String fn = UUID.randomUUID() + "_" + file.getOriginalFilename();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, uploadDir.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
            }
            recipe.setImagePath(fn);
            log.debug("Saved image '{}' to {}", fn, uploadDir);
        }

        // Persist
        recipeService.saveRecipe(recipe);
        log.info("Recipe '{}' persisted (id={})", recipe.getTitle(), recipe.getId());
        return "redirect:/";
    }

    @GetMapping("/recipes/{id}")
    public String details(@PathVariable Long id, Model m) {
        log.debug("Showing details for recipe id={}", id);
        m.addAttribute("recipe", recipeService.getRecipeById(id));
        return "recipe-details";
    }

    @PostMapping("/recipes/{id}/rate")
    public String rateRecipe(@PathVariable Long id,
                             @RequestParam int stars,
                             Principal principal) {
        if (principal == null) {
            log.warn("Anonymous tried to rate recipe id={} → redirect to login", id);
            return "redirect:/login";
        }
        log.info("User '{}' rates recipe id={} with {} stars", principal.getName(), id, stars);
        recipeService.rateRecipe(id, stars, principal.getName());
        return "redirect:/recipes/" + id;
    }

    @GetMapping("/my-recipes")
    public String myRecipes(Model m, Principal principal) {
        if (principal == null) {
            log.warn("Anonymous tried to view /my-recipes");
            return "redirect:/login";
        }
        log.debug("Loading My Recipes for '{}'", principal.getName());
        List<Recipe> mine = recipeService.getRecipesByOwner(principal.getName());
        m.addAttribute("recipes", mine);
        return "my-recipes";
    }

    @PostMapping("/recipes/{id}/delete")
    public String deleteRecipe(@PathVariable Long id, Principal principal) {
        log.info("User '{}' deleting recipe id={}", principal == null ? "ANON": principal.getName(), id);
        recipeService.deleteRecipe(id, principal.getName());
        return "redirect:/my-recipes";
    }
}
