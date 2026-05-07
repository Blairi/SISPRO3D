package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.DeliverableDTO;
import mx.unam.dgtic.entities.DeliverableEntity;
import mx.unam.dgtic.mapper.DeliverableMapper;
import mx.unam.dgtic.repository.IDeliverableRepository;
import mx.unam.dgtic.repository.impl.DeliverableRepository;
import mx.unam.dgtic.service.DeliverableService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DeliverableServiceImpl implements DeliverableService {

    private final IDeliverableRepository deliverableRepository;

    public DeliverableServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.deliverableRepository = new DeliverableRepository(em);
    }

    @Override
    public List<DeliverableDTO> findAll() {
        return DeliverableMapper.toDtoList(deliverableRepository.findAll());
    }

    @Override
    public DeliverableDTO findById(Integer id) {
        return DeliverableMapper.toDTO(deliverableRepository.findById(id));
    }

    @Override
    public DeliverableDTO create(DeliverableDTO dto) {
        DeliverableEntity entity = DeliverableMapper.toEntity(dto);
        deliverableRepository.save(entity);
        return DeliverableMapper.toDTO(entity);
    }

    @Override
    public DeliverableDTO update(Integer id, DeliverableDTO dto) {
        DeliverableEntity existingDeliverable = deliverableRepository.findById(id);
        if (existingDeliverable == null) {
            throw new RuntimeException("Deliverable no encontrado con id: " + id);
        }

        DeliverableEntity deliverableEntity = DeliverableMapper.toEntity(dto);
        deliverableEntity.setId(id);
        deliverableRepository.update(deliverableEntity);
        return DeliverableMapper.toDTO(deliverableEntity);
    }

    @Override
    public void delete(Integer id) {
        DeliverableEntity existingDeliverable = deliverableRepository.findById(id);
        if (existingDeliverable == null) {
            throw new RuntimeException("Deliverable no encontrado con id: " + id);
        }
        deliverableRepository.delete(existingDeliverable);
    }
}
