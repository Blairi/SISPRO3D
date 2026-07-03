package com.sispro3d.unam.review.dto;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.user.dto.ClientDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private int id;
    private int rating; // 1 a 5
    private String comment;
    private ClientDTO client;
    private OfferedServiceDTO offeredService;
    private LocalDateTime createdAt;

    public ReviewDTO(int id) {
        this.id = id;
    }
}
