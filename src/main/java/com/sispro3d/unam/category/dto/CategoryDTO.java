package com.sispro3d.unam.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private int id;
    private String name;
    private String description;

    public CategoryDTO(int id) {
        this.id = id;
    }
}
