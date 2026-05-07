package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.ExpertEntity;
import mx.unam.dgtic.repository.IExpertRepository;
import java.util.List;

public class ExpertRepository implements IExpertRepository {
    private final EntityManager em;

    public ExpertRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(ExpertEntity entity) {
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
    public void update(ExpertEntity entity) {
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
    public void delete(ExpertEntity entity) {
        try {
            em.getTransaction().begin();
            ExpertEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExpertEntity findById(Integer id) {
        try {
            return em.find(ExpertEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExpertEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM ExpertEntity b", ExpertEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
