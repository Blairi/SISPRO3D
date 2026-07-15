package com.sispro3d.unam.review.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Integer rating;
    private String comment;
    private Long clientId;
    private Long offeredServiceId;
}
