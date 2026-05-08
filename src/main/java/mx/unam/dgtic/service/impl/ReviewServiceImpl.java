
package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.ReviewDTO;
import mx.unam.dgtic.entities.ReviewEntity;
import mx.unam.dgtic.mapper.ReviewMapper;
import mx.unam.dgtic.repository.IReviewRepository;
import mx.unam.dgtic.repository.impl.ReviewRepository;
import mx.unam.dgtic.service.ReviewService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ReviewServiceImpl implements ReviewService {

    private final IReviewRepository reviewRepository;

    public ReviewServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.reviewRepository = new ReviewRepository(em);
    }

    @Override
    public List<ReviewDTO> findAll() {
        return ReviewMapper.toDtoList(reviewRepository.findAll());
    }

    @Override
    public ReviewDTO findById(Integer id) {
        ReviewEntity existingReview = reviewRepository.findById(id);
        if (existingReview == null) {
            throw new RuntimeException("Reseña no encontrada con id: " + id);
        }
        return ReviewMapper.toDTO(existingReview);
    }

    @Override
    public ReviewDTO create(ReviewDTO dto) {
        ReviewEntity entity = ReviewMapper.toEntity(dto);
        reviewRepository.save(entity);
        return ReviewMapper.toDTO(entity);
    }

    @Override
    public ReviewDTO update(Integer id, ReviewDTO dto) {
        ReviewEntity existingReview = reviewRepository.findById(id);
        if (existingReview == null) {
            throw new RuntimeException("Reseña no encontrada con id: " + id);
        }
        ReviewEntity reviewEntity = ReviewMapper.toEntity(dto);
        reviewEntity.setId(id);
        reviewRepository.update(reviewEntity);
        return ReviewMapper.toDTO(reviewEntity);
    }

    @Override
    public void delete(Integer id) {
        ReviewEntity existingReview = reviewRepository.findById(id);
        if (existingReview == null) {
            throw new RuntimeException("Reseña no encontrada con id: " + id);
        }
        reviewRepository.delete(existingReview);
    }
}
