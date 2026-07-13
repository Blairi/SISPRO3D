package com.sispro3d.unam.review.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private int id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
