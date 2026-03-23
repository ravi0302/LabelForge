package com.labelforge.dto;

import com.labelforge.model.Category;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public static CategoryResponse from(Category c) {
        CategoryResponse r = new CategoryResponse();
        r.id        = c.getId();
        r.name      = c.getName();
        r.createdAt = c.getCreatedAt();
        return r;
    }
}