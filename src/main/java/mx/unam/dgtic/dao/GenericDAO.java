package mx.unam.dgtic.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T> {
    List<T> findAll();
    Optional<T> findById(int id);
    int insert(T t);
    void update(T t);
    void delete(int id);
}
