package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.WorkOrderDTO;
import mx.unam.dgtic.entities.WorkOrderEntity;
import mx.unam.dgtic.mapper.WorkOrderMapper;
import mx.unam.dgtic.repository.IWorkOrderRepository;
import mx.unam.dgtic.repository.impl.WorkOrderRepository;
import mx.unam.dgtic.service.WorkOrderService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class WorkOrderServiceImpl implements WorkOrderService {

    private final IWorkOrderRepository workOrderRepository;

    public WorkOrderServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.workOrderRepository = new WorkOrderRepository(em);
    }

    @Override
    public List<WorkOrderDTO> findAll() {
        return WorkOrderMapper.toDtoList(workOrderRepository.findAll());
    }

    @Override
    public WorkOrderDTO findById(Integer id) {
        WorkOrderEntity existingWorkOrder = workOrderRepository.findById(id);
        if (existingWorkOrder == null) {
            throw new RuntimeException("Orden de trabajo no encontrada con id: " + id);
        }
        return WorkOrderMapper.toDTO(existingWorkOrder);
    }

    @Override
    public WorkOrderDTO create(WorkOrderDTO dto) {
        WorkOrderEntity entity = WorkOrderMapper.toEntity(dto);
        workOrderRepository.save(entity);
        return WorkOrderMapper.toDTO(entity);
    }

    @Override
    public WorkOrderDTO update(Integer id, WorkOrderDTO dto) {
        WorkOrderEntity existingWorkOrder = workOrderRepository.findById(id);
        if (existingWorkOrder == null) {
            throw new RuntimeException("Orden de trabajo no encontrada con id: " + id);
        }

        WorkOrderEntity workOrderEntity = WorkOrderMapper.toEntity(dto);
        workOrderEntity.setId(id);
        workOrderRepository.update(workOrderEntity);
        return WorkOrderMapper.toDTO(workOrderEntity);
    }

    @Override
    public void delete(Integer id) {
        WorkOrderEntity existingWorkOrder = workOrderRepository.findById(id);
        if (existingWorkOrder == null) {
            throw new RuntimeException("Orden de trabajo no encontrada con id: " + id);
        }
        workOrderRepository.delete(existingWorkOrder);
    }
}
