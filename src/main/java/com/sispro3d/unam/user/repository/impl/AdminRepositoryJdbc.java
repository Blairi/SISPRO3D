package com.sispro3d.unam.user.repository.impl;

import com.sispro3d.unam.user.dao.AdminJdbcDAO;
import com.sispro3d.unam.user.domain.Admin;
import com.sispro3d.unam.user.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepositoryJdbc implements AdminRepository {

    @Autowired
    private AdminJdbcDAO dao;

    @Override
    public Admin save(Admin admin) {
        dao.insert(admin);
        return admin;
    }

    @Override
    public List<Admin> saveAll(Iterable<Admin> entities) {
        List<Admin> result = new ArrayList<>();
        for (Admin entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Admin> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Admin> findAll() {
        return dao.findAll();
    }

    @Override
    public Admin update(Admin admin) {
        dao.update(admin);
        return admin;
    }

    @Override
    public void delete(Admin admin) {
        dao.delete(admin.getAccount().getIdUser());
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
