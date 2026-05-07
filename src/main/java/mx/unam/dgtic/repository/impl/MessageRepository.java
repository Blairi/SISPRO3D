package mx.unam.dgtic.repository.impl;

import jakarta.persistence.EntityManager;
import mx.unam.dgtic.entities.MessageEntity;
import mx.unam.dgtic.repository.IMessageRepository;
import java.util.List;

public class MessageRepository implements IMessageRepository {
    private final EntityManager em;

    public MessageRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(MessageEntity entity) {
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
    public void update(MessageEntity entity) {
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
    public void delete(MessageEntity entity) {
        try {
            em.getTransaction().begin();
            MessageEntity toRemove = em.contains(entity) ? entity : em.merge(entity);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageEntity findById(Integer id) {
        try {
            return em.find(MessageEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MessageEntity> findAll() {
        try {
            return em.createQuery("SELECT b FROM MessageEntity b", MessageEntity.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
