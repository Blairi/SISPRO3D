package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.DeliverableEntity;
import mx.unam.dgtic.repository.IDeliverableRepository;
import java.util.List;

public class DeliverableRepository implements IDeliverableRepository {
    private final EntityManager em;

    public DeliverableRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(DeliverableEntity entity) {
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
    public void update(DeliverableEntity entity) {
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
    public void delete(DeliverableEntity entity) {
        try {
            em.getTransaction().begin();
            DeliverableEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public DeliverableEntity findById(Integer id) {
        try {
            return em.find(DeliverableEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DeliverableEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM DeliverableEntity b", DeliverableEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
