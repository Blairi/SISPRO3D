package com.sispro3d.unam.user.repository.impl;

import com.sispro3d.unam.user.dao.ClientJdbcDAO;
import com.sispro3d.unam.user.domain.Client;
import com.sispro3d.unam.user.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ClientRepositoryJdbc implements ClientRepository {

    @Autowired
    private ClientJdbcDAO dao;

    @Override
    public Client save(Client client) {
        dao.insert(client);
        return client;
    }

    @Override
    public List<Client> saveAll(Iterable<Client> entities) {
        List<Client> result = new ArrayList<>();
        for (Client entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Client> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Client> findAll() {
        return dao.findAll();
    }

    @Override
    public Client update(Client client) {
        dao.update(client);
        return client;
    }

    @Override
    public void delete(Client client) {
        dao.delete(client.getAccount().getIdUser());
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
