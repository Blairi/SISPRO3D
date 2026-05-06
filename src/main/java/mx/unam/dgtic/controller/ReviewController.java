package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.ReviewJdbcDAO;
import mx.unam.dgtic.dto.ReviewDTO;
import mx.unam.dgtic.service.ReviewService;
import mx.unam.dgtic.service.impl.ReviewServiceImpl;

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
