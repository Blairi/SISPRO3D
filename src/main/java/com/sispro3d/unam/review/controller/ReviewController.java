package com.sispro3d.unam.review.controller;

import com.sispro3d.unam.review.dao.ReviewJdbcDAO;
import com.sispro3d.unam.review.dto.ReviewDTO;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.review.service.impl.ReviewServiceImpl;

import java.util.Optional;

public class ReviewController {
    private ReviewService reviewService;

    public ReviewController() {
        this.reviewService = new ReviewServiceImpl(new ReviewJdbcDAO());
    }

    public void displayReview(int id) {
        System.out.println("Displaying review with id = " + id);
        Optional<ReviewDTO> reviewDTO = reviewService.findById(id);
        System.out.println("reviewDTO = " + reviewDTO);
    }

    public void displayAllReviews() {
        System.out.println("Displaying all reviews:");
        reviewService.findAll().forEach(System.out::println);
    }
}
