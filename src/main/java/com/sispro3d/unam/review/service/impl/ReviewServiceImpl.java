package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ReviewServiceImpl implements ReviewService {

    @Override
    public List<ReviewResponse> findAll() {
        return List.of();
    }

    @Override
    public Optional<ReviewResponse> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public ReviewResponse create(ReviewRequest request) {
        return null;
    }

    @Override
    public ReviewResponse update(Long aLong, ReviewRequest request) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }
}
