package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.AdminEntity;
import mx.unam.dgtic.repository.IAdminRepository;
import java.util.List;

public class AdminRepository implements IAdminRepository {
    private final EntityManager em;

    public AdminRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(AdminEntity entity) {
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
    public void update(AdminEntity entity) {
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
    public void delete(AdminEntity entity) {
        try {
            em.getTransaction().begin();
            AdminEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AdminEntity findById(Integer id) {
        try {
            return em.find(AdminEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AdminEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM AdminEntity b", AdminEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
