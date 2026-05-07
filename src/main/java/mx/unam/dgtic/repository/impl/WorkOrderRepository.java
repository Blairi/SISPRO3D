package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.WorkOrderEntity;
import mx.unam.dgtic.repository.IWorkOrderRepository;
import java.util.List;

public class WorkOrderRepository implements IWorkOrderRepository {
    private final EntityManager em;

    public WorkOrderRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(WorkOrderEntity entity) {
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
    public void update(WorkOrderEntity entity) {
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
    public void delete(WorkOrderEntity entity) {
        try {
            em.getTransaction().begin();
            WorkOrderEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkOrderEntity findById(Integer id) {
        try {
            return em.find(WorkOrderEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<WorkOrderEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM WorkOrderEntity b", WorkOrderEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
