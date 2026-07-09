package com.sispro3d.unam.user.repository.impl;

import com.sispro3d.unam.user.dao.AccountJdbcDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryJdbc implements AccountRepository {

    @Autowired
    private AccountJdbcDAO dao;

    @Override
    public Account save(Account account) {
        dao.insert(account);
        return account;
    }

    @Override
    public List<Account> saveAll(Iterable<Account> entities) {
        List<Account> result = new ArrayList<>();
        for (Account entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Account> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Account> findAll() {
        return dao.findAll();
    }

    @Override
    public Account update(Account account) {
        dao.update(account);
        return account;
    }

    @Override
    public void delete(Account account) {
        dao.delete(account.getIdUser());
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
