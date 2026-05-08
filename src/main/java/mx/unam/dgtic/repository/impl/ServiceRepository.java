package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.ServiceEntity;
import mx.unam.dgtic.repository.IServiceRepository;
import java.util.List;

public class ServiceRepository implements IServiceRepository {
    private final EntityManager em;

    public ServiceRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(ServiceEntity entity) {
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
    public void update(ServiceEntity entity) {
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
    public void delete(ServiceEntity entity) {
        try {
            em.getTransaction().begin();
            ServiceEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServiceEntity findById(Integer id) {
        try {
            return em.find(ServiceEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM ServiceEntity b", ServiceEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceEntity> findAllServicesFromExpertId(Integer id) {
        return em.createQuery("SELECT s FROM ServiceEntity s WHERE s.expert.id = :id", ServiceEntity.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public boolean isApprovedByAdmin(Integer idService) {
        Long count = em.createQuery(
                        "SELECT COUNT(s) FROM ServiceEntity s WHERE s.id = :id AND s.admin IS NOT NULL", Long.class)
                .setParameter("id", idService)
                .getSingleResult();

        return count > 0;
    }
}
