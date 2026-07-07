package com.sispro3d.unam.core.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Generic data access contract (Repository pattern) for entities
 * of type {@code T} identified by a key of type {@code ID}.
 *
 * @param <T>  the entity type managed by the repository
 * @param <ID> the type of the entity identifier, must be serializable
 */
public interface Repository<T, ID extends Serializable> {

    /**
     * Persists a new entity.
     *
     * @param entity the entity to save, must not be {@code null}
     * @return the persisted entity (may include the generated ID)
     */
    T save(T entity);

    /**
     * Persists a collection of entities in a single operation.
     *
     * @param entities the entities to save
     * @return the persisted entities
     */
    List<T> saveAll(Iterable<T> entities);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the entity identifier, must not be {@code null}
     * @return an {@link Optional} containing the entity if found, empty otherwise
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities.
     *
     * @return a list of entities (never {@code null}, may be empty)
     */
    List<T> findAll();

    /**
     * Updates an existing entity.
     *
     * @param entity the entity with updated data
     * @return the updated entity
     * @throws IllegalArgumentException if the entity does not exist
     */
    T update(T entity);

    /**
     * Deletes an entity.
     *
     * @param entity the entity to delete, must not be {@code null}
     */
    void delete(T entity);

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the identifier of the entity to delete
     */
    void deleteById(ID id);

    /**
     * Checks whether an entity with the given identifier exists.
     *
     * @param id the identifier to check
     * @return {@code true} if it exists, {@code false} otherwise
     */
    boolean existsById(ID id);

    /**
     * Counts the total number of entities.
     *
     * @return the number of stored entities
     */
    long count();
}