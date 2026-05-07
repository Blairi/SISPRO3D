package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.ClientEntity;
import mx.unam.dgtic.repository.IClientRepository;
import java.util.List;

public class ClientRepository implements IClientRepository {
    private final EntityManager em;

    public ClientRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(ClientEntity entity) {
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
    public void update(ClientEntity entity) {
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
    public void delete(ClientEntity entity) {
        try {
            em.getTransaction().begin();
            ClientEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClientEntity findById(Integer id) {
        try {
            return em.find(ClientEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ClientEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM ClientEntity b", ClientEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
