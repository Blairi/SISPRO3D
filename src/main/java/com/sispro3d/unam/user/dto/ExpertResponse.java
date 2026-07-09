package com.sispro3d.unam.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertResponse {
    private int id;
    private String name;
    private String lastName;
    private String email;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private int yearsExperience;
}
