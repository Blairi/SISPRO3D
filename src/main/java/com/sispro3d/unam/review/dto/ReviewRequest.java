package com.sispro3d.unam.review.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private int rating;
    private String comment;
    private int clientId;
    private int offeredServiceId;
}
