package mx.unam.dgtic.repository;

import java.util.List;

public interface IRepository<T, ID> {
    void save(T entity);
    void update(T entity);
    void delete(T entity);
    T findById(ID id);
    List<T> findAll();
}

