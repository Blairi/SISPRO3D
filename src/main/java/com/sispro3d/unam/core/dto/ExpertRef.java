package com.sispro3d.unam.core.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertRef {
    private int id;
    private String name;
    private String lastName;
    private String email;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private int yearsExperience;
}
