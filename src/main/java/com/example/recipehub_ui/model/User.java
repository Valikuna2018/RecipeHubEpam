package com.example.recipehub_ui.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false, length=50)
    private String username;

    @Column(nullable=false)
    private String password;

    @OneToMany(mappedBy="owner", cascade=CascadeType.ALL)
    private Set<Recipe> recipes;

    @OneToMany(mappedBy="rater", cascade=CascadeType.ALL)
    private Set<Rating> ratings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(Set<Recipe> recipes) {
        this.recipes = recipes;
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }
}
