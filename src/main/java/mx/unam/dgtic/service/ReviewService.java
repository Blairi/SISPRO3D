package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.ReviewDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<ReviewDTO> findAll();
    Optional<ReviewDTO> findById(int id);
    ReviewDTO create(ReviewDTO dto);
    ReviewDTO update(int id, ReviewDTO dto);
    void delete(int id);
}
