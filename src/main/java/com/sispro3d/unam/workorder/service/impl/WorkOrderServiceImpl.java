package com.sispro3d.unam.workorder.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.dto.QuoteDTO;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.dto.WorkOrderDTO;
import com.sispro3d.unam.workorder.service.WorkOrderService;

import java.util.List;
import java.util.Optional;

public class WorkOrderServiceImpl implements WorkOrderService {

    private final GenericDAO<WorkOrder> workOrderDAO;

    public WorkOrderServiceImpl(GenericDAO<WorkOrder> workOrderDAO) {
        this.workOrderDAO = workOrderDAO;
    }

    @Override
    public List<WorkOrderDTO> findAll() {
        return workOrderDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<WorkOrderDTO> findById(int id) {
        return workOrderDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public WorkOrderDTO create(WorkOrderDTO dto) {
        WorkOrder workOrder = toEntity(dto);
        int generatedId = workOrderDAO.insert(workOrder);
        workOrder.setId(generatedId);
        return toResponseDTO(workOrder);
    }

    @Override
    public WorkOrderDTO update(int id, WorkOrderDTO dto) {
        workOrderDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada con id: " + id));

        WorkOrder workOrder = toEntity(dto);
        workOrder.setId(id);
        workOrderDAO.update(workOrder);
        return toResponseDTO(workOrder);
    }

    @Override
    public void delete(int id) {
        workOrderDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada con id: " + id));
        workOrderDAO.delete(id);
    }

    private WorkOrder toEntity(WorkOrderDTO dto) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setStatus(dto.getStatus());
        workOrder.setStartedAt(dto.getStartedAt());
        workOrder.setCompletedAt(dto.getCompletedAt());

        if (dto.getQuote() != null) {
            Quote quote = new Quote(dto.getQuote().getId());
            workOrder.setQuote(quote);
        }

        return workOrder;
    }

    private WorkOrderDTO toResponseDTO(WorkOrder workOrder) {
        WorkOrderDTO dto = new WorkOrderDTO();
        dto.setId(workOrder.getId());
        dto.setStatus(workOrder.getStatus());
        dto.setStartedAt(workOrder.getStartedAt());
        dto.setCompletedAt(workOrder.getCompletedAt());
        dto.setCreatedAt(workOrder.getCreatedAt());

        if (workOrder.getQuote() != null) {
            QuoteDTO quoteDTO = new QuoteDTO();
            quoteDTO.setId(workOrder.getQuote().getId());
            quoteDTO.setStatus(workOrder.getQuote().getStatus());
            quoteDTO.setTotalAmount(workOrder.getQuote().getTotalAmount());
            quoteDTO.setValidUntil(workOrder.getQuote().getValidUntil());
            quoteDTO.setDescription(workOrder.getQuote().getDescription());
            quoteDTO.setCreatedAt(workOrder.getQuote().getCreatedAt());
            dto.setQuote(quoteDTO);
        }

        return dto;
    }
}
