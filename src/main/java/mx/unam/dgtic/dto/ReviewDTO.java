package mx.unam.dgtic.dto;

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
public class ReviewDTO {
    private int id;
    private int rating; // 1 a 5
    private String comment;
    private ClientDTO client;
    private ServiceDTO service;
    private LocalDateTime createdAt;

    public ReviewDTO(int id) {
        this.id = id;
    }
}
