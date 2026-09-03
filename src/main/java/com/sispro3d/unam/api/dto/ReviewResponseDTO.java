package com.sispro3d.unam.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private Long clientId;
    private String clientName;
    private Long offeredServiceId;
    private LocalDateTime createdAt;
}
