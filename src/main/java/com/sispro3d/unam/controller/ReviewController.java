package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.ReviewJdbcDAO;
import com.sispro3d.unam.dto.ReviewDTO;
import com.sispro3d.unam.service.ReviewService;
import com.sispro3d.unam.service.impl.ReviewServiceImpl;

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
