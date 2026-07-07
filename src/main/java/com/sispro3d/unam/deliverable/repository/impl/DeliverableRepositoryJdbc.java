package com.sispro3d.unam.deliverable.repository.impl;

import com.sispro3d.unam.deliverable.dao.DeliverableJdbcDAO;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DeliverableRepositoryJdbc implements DeliverableRepository {

    @Autowired
    private DeliverableJdbcDAO dao;

    @Override
    public Deliverable save(Deliverable deliverable) {
        dao.insert(deliverable);
        return deliverable;
    }

    @Override
    public List<Deliverable> saveAll(Iterable<Deliverable> entities) {
        List<Deliverable> result = new ArrayList<>();
        for (Deliverable entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Deliverable> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Deliverable> findAll() {
        return dao.findAll();
    }

    @Override
    public Deliverable update(Deliverable deliverable) {
        dao.update(deliverable);
        return deliverable;
    }

    @Override
    public void delete(Deliverable deliverable) {
        dao.delete(deliverable.getId());
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
