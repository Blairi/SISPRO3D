package com.sispro3d.unam.review.dto;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
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
    private AccountRef client;
    private OfferedServiceRef offeredService;
    private LocalDateTime createdAt;
}
