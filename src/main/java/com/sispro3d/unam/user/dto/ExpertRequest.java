package com.sispro3d.unam.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertRequest {
    private int accountId;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private int yearsExperience;
}
