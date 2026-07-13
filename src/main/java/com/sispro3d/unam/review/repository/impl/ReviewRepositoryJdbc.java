package com.sispro3d.unam.review.repository.impl;

import com.sispro3d.unam.review.dao.ReviewJdbcDAO;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryJdbc implements ReviewRepository {

    @Autowired
    private ReviewJdbcDAO dao;

    @Override
    public Review save(Review review) {
        int generatedId = dao.insert(review);
        review.setId(generatedId);
        return review;
    }

    @Override
    public List<Review> saveAll(Iterable<Review> entities) {
        List<Review> result = new ArrayList<>();
        for (Review entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Review> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Review> findAll() {
        return dao.findAll();
    }

    @Override
    public Review update(Review review) {
        dao.update(review);
        return review;
    }

    @Override
    public void delete(Review review) {
        dao.delete(review.getId());
    }

    @Override
    public void deleteById(Integer id) {
        dao.delete(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return dao.findById(id).isPresent();
    }

    @Override
    public long count() {
        return dao.findAll().size();
    }
}
