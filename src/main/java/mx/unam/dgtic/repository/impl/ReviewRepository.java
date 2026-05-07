package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.ReviewEntity;
import mx.unam.dgtic.repository.IReviewRepository;
import java.util.List;

public class ReviewRepository implements IReviewRepository {
    private final EntityManager em;

    public ReviewRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(ReviewEntity entity) {
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ReviewEntity entity) {
        try {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(ReviewEntity entity) {
        try {
            em.getTransaction().begin();
            ReviewEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReviewEntity findById(Integer id) {
        try {
            return em.find(ReviewEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReviewEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM ReviewEntity b", ReviewEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
