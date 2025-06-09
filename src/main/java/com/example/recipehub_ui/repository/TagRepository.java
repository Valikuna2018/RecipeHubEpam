package com.example.recipehub_ui.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.recipehub_ui.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String name);
}
