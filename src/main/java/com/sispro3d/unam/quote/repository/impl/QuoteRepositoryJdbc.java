package com.sispro3d.unam.quote.repository.impl;

import com.sispro3d.unam.quote.dao.QuoteJdbcDAO;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class QuoteRepositoryJdbc implements QuoteRepository {

    @Autowired
    private QuoteJdbcDAO dao;

    @Override
    public Quote save(Quote quote) {
        int generatedId = dao.insert(quote);
        quote.setId(generatedId);
        return quote;
    }

    @Override
    public List<Quote> saveAll(Iterable<Quote> entities) {
        List<Quote> result = new ArrayList<>();
        for (Quote entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Quote> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Quote> findAll() {
        return dao.findAll();
    }

    @Override
    public Quote update(Quote quote) {
        dao.update(quote);
        return quote;
    }

    @Override
    public void delete(Quote quote) {
        dao.delete(quote.getId());
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
