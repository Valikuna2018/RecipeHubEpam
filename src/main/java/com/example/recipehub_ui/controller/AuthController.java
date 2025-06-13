package com.example.recipehub_ui.controller;

import com.example.recipehub_ui.model.User;
import com.example.recipehub_ui.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository repo, PasswordEncoder enc) {
        this.users = repo;
        this.encoder = enc;
    }

    @GetMapping("/register")
    public String showRegister(User user) {
        log.debug("GET /register → showing registration form");
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model m) {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            log.warn("Registration failed: username '{}' already taken", user.getUsername());
            m.addAttribute("error","Username already taken");
            return "register";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        users.save(user);
        log.info("New user registered: {}", user.getUsername());
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required=false) String error,
            @RequestParam(required=false) String registered,
            Model m
    ) {
        if (error != null) {
            m.addAttribute("error","Invalid credentials");
        }
        if (registered != null) {
            m.addAttribute("msg","Registration successful—please log in");
        }
        return "login";
    }
}
