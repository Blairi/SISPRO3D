package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.ThreadEntity;
import mx.unam.dgtic.repository.IThreadRepository;
import java.util.List;

public class ThreadRepository implements IThreadRepository {
    private final EntityManager em;

    public ThreadRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(ThreadEntity entity) {
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
    public void update(ThreadEntity entity) {
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
    public void delete(ThreadEntity entity) {
        try {
            em.getTransaction().begin();
            ThreadEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ThreadEntity findById(Integer id) {
        try {
            return em.find(ThreadEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ThreadEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM ThreadEntity b", ThreadEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
