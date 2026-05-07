package mx.unam.dgtic.service;

import java.util.List;

public interface IService <T, ID> {
    List<T> findAll();
    T findById(ID id);
    T create(T dto);
    T update(ID id, T dto);
    void delete(ID id);
}
