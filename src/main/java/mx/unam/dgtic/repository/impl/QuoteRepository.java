package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.QuoteEntity;
import mx.unam.dgtic.repository.IQuoteRepository;
import java.util.List;

public class QuoteRepository implements IQuoteRepository {
    private final EntityManager em;

    public QuoteRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(QuoteEntity entity) {
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
    public void update(QuoteEntity entity) {
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
    public void delete(QuoteEntity entity) {
        try {
            em.getTransaction().begin();
            QuoteEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public QuoteEntity findById(Integer id) {
        try {
            return em.find(QuoteEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<QuoteEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM QuoteEntity b", QuoteEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
