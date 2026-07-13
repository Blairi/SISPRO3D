package com.sispro3d.unam.review.domain;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private int id;
    private int rating;
    private String comment;
    private Account client;
    private OfferedService offeredService;
    private LocalDateTime createdAt;

    public Review(int id) {
        this.id = id;
    }
}
