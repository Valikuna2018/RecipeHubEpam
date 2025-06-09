package com.example.recipehub_ui.service;

import com.example.recipehub_ui.model.Tag;
import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    Tag findByName(String name);
}
