package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.CategoryEntity;
import mx.unam.dgtic.repository.ICategoryRepository;
import java.util.List;

public class CategoryRepository implements ICategoryRepository {
    private final EntityManager em;

    public CategoryRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(CategoryEntity entity) {
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
    public void update(CategoryEntity entity) {
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
    public void delete(CategoryEntity entity) {
        try {
            em.getTransaction().begin();
            CategoryEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public CategoryEntity findById(Integer id) {
        try {
            return em.find(CategoryEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CategoryEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM CategoryEntity b", CategoryEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
