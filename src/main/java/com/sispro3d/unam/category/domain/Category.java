package com.sispro3d.unam.category.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private int id;
    private String name;
    private String description;

    public Category(int id) {
        this.id = id;
    }
}
