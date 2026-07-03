package com.sispro3d.unam.thread.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.dto.ThreadDTO;
import com.sispro3d.unam.thread.service.ThreadService;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.dto.WorkOrderDTO;

import java.util.List;
import java.util.Optional;

public class ThreadServiceImpl implements ThreadService {

    private final GenericDAO<Thread> threadDAO;

    public ThreadServiceImpl(GenericDAO<Thread> threadDAO) {
        this.threadDAO = threadDAO;
    }

    @Override
    public List<ThreadDTO> findAll() {
        return threadDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<ThreadDTO> findById(int id) {
        return threadDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public ThreadDTO create(ThreadDTO dto) {
        Thread thread = toEntity(dto);
        int generatedId = threadDAO.insert(thread);
        thread.setId(generatedId);
        return toResponseDTO(thread);
    }

    @Override
    public ThreadDTO update(int id, ThreadDTO dto) {
        threadDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Hilo no encontrado con id: " + id));

        Thread thread = toEntity(dto);
        thread.setId(id);
        threadDAO.update(thread);
        return toResponseDTO(thread);
    }

    @Override
    public void delete(int id) {
        threadDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Hilo no encontrado con id: " + id));
        threadDAO.delete(id);
    }

    private Thread toEntity(ThreadDTO dto) {
        Thread thread = new Thread();

        if (dto.getWorkOrder() != null) {
            WorkOrder workOrder = new WorkOrder(dto.getWorkOrder().getId());
            thread.setWorkOrder(workOrder);
        }

        return thread;
    }

    private ThreadDTO toResponseDTO(Thread thread) {
        ThreadDTO dto = new ThreadDTO();
        dto.setId(thread.getId());

        if (thread.getWorkOrder() != null) {
            WorkOrderDTO woDTO = new WorkOrderDTO();
            woDTO.setId(thread.getWorkOrder().getId());
            woDTO.setStatus(thread.getWorkOrder().getStatus());
            woDTO.setStartedAt(thread.getWorkOrder().getStartedAt());
            woDTO.setCompletedAt(thread.getWorkOrder().getCompletedAt());
            woDTO.setCreatedAt(thread.getWorkOrder().getCreatedAt());
            dto.setWorkOrder(woDTO);
        }

        return dto;
    }
}
