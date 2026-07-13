package com.sispro3d.unam.review.controller;

import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    public void displayReview(long id) {
        System.out.println("Displaying review with id = " + id);
        Optional<ReviewResponse> review = reviewService.findById(id);
        System.out.println("review = " + review);
    }

    public void displayAllReviews() {
        System.out.println("Displaying all reviews:");
        List<ReviewResponse> reviews = reviewService.findAll();
        reviews.forEach(System.out::println);
    }
}
