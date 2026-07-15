package com.sispro3d.unam.core.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Generic service contract (business layer) for entities of type {@code Res}
 * identified by a key of type {@code ID}.
 * <p>
 * Acts as the boundary between the presentation layer and the persistence
 * layer, applying business rules and validation before delegating to the
 * corresponding repository.
 *
 * @param <Req> the request DTO type (input for create/update operations)
 * @param <Res> the response DTO type (output returned to the caller)
 * @param <ID>  the type of the entity identifier, must be serializable
 */
public interface CrudService<Req, Res, ID extends Serializable> {

    /**
     * Retrieves all entities.
     *
     * @return a list of entities (never {@code null}, may be empty)
     */
    List<Res> findAll();

    /**
     * Finds an entity by its identifier.
     *
     * @param id the entity identifier, must not be {@code null}
     * @return an {@link Optional} containing the entity if found, empty otherwise
     */
    Optional<Res> findById(ID id);

    /**
     * Creates a new entity.
     *
     * @param request the data used to create the entity, must not be {@code null}
     * @return the created entity, including its generated identifier
     * @throws IllegalArgumentException if the provided data is invalid
     */
    Res create(Req request);

    /**
     * Updates an existing entity.
     *
     * @param id      the identifier of the entity to update, must not be {@code null}
     * @param request the updated data, must not be {@code null}
     * @return the updated entity
     * @throws java.util.NoSuchElementException if no entity exists with the given id
     * @throws IllegalArgumentException          if the provided data is invalid
     */
    Res update(ID id, Req request);

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the identifier of the entity to delete, must not be {@code null}
     * @throws java.util.NoSuchElementException if no entity exists with the given id
     */
    void delete(ID id);

    /**
     * Checks whether an entity exists with the given identifier.
     *
     * @param id the identifier to check, must not be {@code null}
     * @return {@code true} if it exists, {@code false} otherwise
     */
    boolean existsById(ID id);
}