package com.sispro3d.unam.core.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRef {
    private int id;
    private String name;
    private String description;
}
