package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dao.GenericDAO;
import mx.unam.dgtic.domain.Deliverable;
import mx.unam.dgtic.domain.WorkOrder;
import mx.unam.dgtic.dto.DeliverableDTO;
import mx.unam.dgtic.dto.WorkOrderDTO;
import mx.unam.dgtic.service.DeliverableService;

import java.util.List;
import java.util.Optional;

public class DeliverableServiceImpl implements DeliverableService {

    private final GenericDAO<Deliverable> deliverableDAO;

    public DeliverableServiceImpl(GenericDAO<Deliverable> deliverableDAO) {
        this.deliverableDAO = deliverableDAO;
    }

    @Override
    public List<DeliverableDTO> findAll() {
        return deliverableDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<DeliverableDTO> findById(int id) {
        return deliverableDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public DeliverableDTO create(DeliverableDTO dto) {
        Deliverable deliverable = toEntity(dto);
        int generatedId = deliverableDAO.insert(deliverable);
        deliverable.setId(generatedId);
        return toResponseDTO(deliverable);
    }

    @Override
    public DeliverableDTO update(int id, DeliverableDTO dto) {
        deliverableDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Entregable no encontrado con id: " + id));

        Deliverable deliverable = toEntity(dto);
        deliverable.setId(id);
        deliverableDAO.update(deliverable);
        return toResponseDTO(deliverable);
    }

    @Override
    public void delete(int id) {
        deliverableDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Entregable no encontrado con id: " + id));
        deliverableDAO.delete(id);
    }

    private Deliverable toEntity(DeliverableDTO dto) {
        Deliverable deliverable = new Deliverable();
        deliverable.setName(dto.getName());
        deliverable.setUrlFile(dto.getUrlFile());
        deliverable.setFileType(dto.getFileType());

        if (dto.getWorkOrder() != null) {
            WorkOrder workOrder = new WorkOrder(dto.getWorkOrder().getId());
            deliverable.setWorkOrder(workOrder);
        }

        return deliverable;
    }

    private DeliverableDTO toResponseDTO(Deliverable deliverable) {
        DeliverableDTO dto = new DeliverableDTO();
        dto.setId(deliverable.getId());
        dto.setName(deliverable.getName());
        dto.setUrlFile(deliverable.getUrlFile());
        dto.setFileType(deliverable.getFileType());
        dto.setCreatedAt(deliverable.getCreatedAt());

        if (deliverable.getWorkOrder() != null) {
            WorkOrderDTO woDTO = new WorkOrderDTO();
            woDTO.setId(deliverable.getWorkOrder().getId());
            woDTO.setStatus(deliverable.getWorkOrder().getStatus());
            woDTO.setStartedAt(deliverable.getWorkOrder().getStartedAt());
            woDTO.setCompletedAt(deliverable.getWorkOrder().getCompletedAt());
            woDTO.setCreatedAt(deliverable.getWorkOrder().getCreatedAt());
            dto.setWorkOrder(woDTO);
        }

        return dto;
    }
}
