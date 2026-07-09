package com.sispro3d.unam.user.repository.impl;

import com.sispro3d.unam.user.dao.ExpertJdbcDAO;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.repository.ExpertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExpertRepositoryJdbc implements ExpertRepository {

    @Autowired
    private ExpertJdbcDAO dao;

    @Override
    public Expert save(Expert expert) {
        dao.insert(expert);
        return expert;
    }

    @Override
    public List<Expert> saveAll(Iterable<Expert> entities) {
        List<Expert> result = new ArrayList<>();
        for (Expert entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Expert> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Expert> findAll() {
        return dao.findAll();
    }

    @Override
    public Expert update(Expert expert) {
        dao.update(expert);
        return expert;
    }

    @Override
    public void delete(Expert expert) {
        dao.delete(expert.getAccount().getIdUser());
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
