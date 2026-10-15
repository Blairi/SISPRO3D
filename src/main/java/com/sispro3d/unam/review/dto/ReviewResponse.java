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
    private Long id;
    private Integer rating;
    private String comment;
    private Long clientId;
    private Long offeredServiceId;
    private LocalDateTime createdAt;
}
