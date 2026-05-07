package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.AccountEntity;
import mx.unam.dgtic.repository.IAccountRepository;

import java.util.List;

public class AccountRepository implements IAccountRepository {

    private final EntityManager em;

    public AccountRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(AccountEntity entity) {
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
    public void update(AccountEntity entity) {
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
    public void delete(AccountEntity entity) {
        try {
            em.getTransaction().begin();
            AccountEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccountEntity findById(Integer id) {
        try {
            return em.find(AccountEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AccountEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM AccountEntity b", AccountEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
