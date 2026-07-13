package com.sispro3d.unam.message.repository.impl;

import com.sispro3d.unam.message.dao.MessageJdbcDAO;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepositoryJdbc implements MessageRepository {

    @Autowired
    private MessageJdbcDAO dao;

    @Override
    public Message save(Message message) {
        int generatedId = dao.insert(message);
        message.setId(generatedId);
        return message;
    }

    @Override
    public List<Message> saveAll(Iterable<Message> entities) {
        List<Message> result = new ArrayList<>();
        for (Message entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Message> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return dao.findAll();
    }

    @Override
    public Message update(Message message) {
        dao.update(message);
        return message;
    }

    @Override
    public void delete(Message message) {
        dao.delete(message.getId());
    }

    @Override
    public void deleteById(Integer id) {
        dao.delete(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return dao.findById(id).isPresent();
    }

    @Override
    public long count() {
        return dao.findAll().size();
    }
}
