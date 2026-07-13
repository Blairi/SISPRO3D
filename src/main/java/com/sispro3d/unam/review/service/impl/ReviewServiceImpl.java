package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.review.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<ReviewResponse> findAll() {
        return null;
    }

    @Override
    public Optional<ReviewResponse> findById(Long id) {
        return null;
    }

    @Override
    public ReviewResponse create(ReviewRequest request) {
        return null;
    }

    @Override
    public ReviewResponse update(Long id, ReviewRequest request) {
        int pk = id.intValue();
        reviewRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));

        return null;
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        reviewRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));
        reviewRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return reviewRepository.existsById(id.intValue());
    }

}
