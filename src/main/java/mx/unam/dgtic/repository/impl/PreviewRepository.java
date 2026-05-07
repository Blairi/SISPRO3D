package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.PreviewEntity;
import mx.unam.dgtic.repository.IPreviewRepository;
import java.util.List;

public class PreviewRepository implements IPreviewRepository {
    private final EntityManager em;

    public PreviewRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(PreviewEntity entity) {
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
    public void update(PreviewEntity entity) {
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
    public void delete(PreviewEntity entity) {
        try {
            em.getTransaction().begin();
            PreviewEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public PreviewEntity findById(Integer id) {
        try {
            return em.find(PreviewEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PreviewEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM PreviewEntity b", PreviewEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
