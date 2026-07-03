package com.sispro3d.unam.review.domain;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private int id;
    private int rating; // 1 a 5
    private String comment;
    private Client client;
    private OfferedService offeredService;
    private LocalDateTime createdAt;

    public Review(int id) {
        this.id = id;
    }
}
