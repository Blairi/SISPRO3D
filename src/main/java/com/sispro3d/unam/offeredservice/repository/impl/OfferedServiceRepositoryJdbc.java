package com.sispro3d.unam.offeredservice.repository.impl;

import com.sispro3d.unam.offeredservice.dao.OfferedServiceJdbcDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OfferedServiceRepositoryJdbc implements OfferedServiceRepository {

    @Autowired
    private OfferedServiceJdbcDAO dao;

    @Override
    public OfferedService save(OfferedService offeredService) {
        int generatedId = dao.insert(offeredService);
        offeredService.setId(generatedId);
        return offeredService;
    }

    @Override
    public List<OfferedService> saveAll(Iterable<OfferedService> entities) {
        List<OfferedService> result = new ArrayList<>();
        for (OfferedService entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<OfferedService> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<OfferedService> findAll() {
        return dao.findAll();
    }

    @Override
    public OfferedService update(OfferedService offeredService) {
        dao.update(offeredService);
        return offeredService;
    }

    @Override
    public void delete(OfferedService offeredService) {
        dao.delete(offeredService.getId());
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
