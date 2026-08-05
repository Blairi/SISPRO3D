package com.sispro3d.unam.workorder.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.mapper.WorkOrderMapper;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    @Autowired
    private WorkOrderRepository workOrderRepository;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private QuoteRepository quoteRepository;

    @Override
    public List<WorkOrderResponse> findAll() {
        return workOrderRepository.findAll().stream()
                .map(workOrderMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<WorkOrderResponse> findById(Long id) {
        return workOrderRepository.findById(id)
                .map(workOrderMapper::toResponse);
    }

    @Override
    public WorkOrderResponse create(WorkOrderRequest request) {
        var client = accountRepository.findById(request.getClientId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", request.getClientId()));

        if (client.getRole() != Role.CLIENT) {
            throw new IllegalStateException("Solo cuentas de tipo CLIENT pueden crear órdenes de trabajo");
        }

        var quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", request.getQuoteId()));

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new IllegalStateException("Solo se pueden crear órdenes a partir de cotizaciones en estado ACCEPTED");
        }
        if (quote.getClient() == null || !request.getClientId().equals(quote.getClient().getIdUser())) {
            throw new IllegalStateException("Solo el cliente propietario de la cotización puede crear la orden");
        }

        WorkOrder order = workOrderMapper.toEntity(request);
        order.setQuote(quote);
        order.setStatus(WorkOrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(order);
        return workOrderMapper.toResponse(saved);
    }

    @Override
    public WorkOrderResponse update(Long id, WorkOrderRequest request) {
        WorkOrder existing = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        if (!accountRepository.existsById(request.getClientId())) {
            throw ResourceNotFoundException.forId("Account (client)", request.getClientId());
        }
        if (!quoteRepository.existsById(request.getQuoteId())) {
            throw ResourceNotFoundException.forId("Quote", request.getQuoteId());
        }

        existing.setQuote(quoteRepository.getReferenceById(request.getQuoteId()));
        WorkOrder updated = workOrderRepository.save(existing);
        return workOrderMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!workOrderRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("WorkOrder", id);
        }
        workOrderRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return workOrderRepository.existsById(id);
    }

    @Override
    public WorkOrderResponse start(Long id, Long expertId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireExpertOwner(order, expertId);

        if (order.getStatus() != WorkOrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden iniciar órdenes en estado PENDING");
        }

        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        order.setStartedAt(LocalDateTime.now());
        return workOrderMapper.toResponse(workOrderRepository.save(order));
    }

    @Override
    public WorkOrderResponse markInReview(Long id, Long expertId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireExpertOwner(order, expertId);

        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Solo se pueden enviar a revisión órdenes en estado IN_PROGRESS");
        }

        order.setStatus(WorkOrderStatus.IN_REVIEW);
        return workOrderMapper.toResponse(workOrderRepository.save(order));
    }

    @Override
    public WorkOrderResponse requestChanges(Long id, Long clientId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireClientOwner(order, clientId);

        if (order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            throw new IllegalStateException("Solo se pueden solicitar cambios en órdenes en estado IN_REVIEW");
        }

        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        return workOrderMapper.toResponse(workOrderRepository.save(order));
    }

    @Override
    public WorkOrderResponse complete(Long id, Long clientId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireClientOwner(order, clientId);

        if (order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            throw new IllegalStateException("Solo se pueden completar órdenes en estado IN_REVIEW");
        }

        order.setStatus(WorkOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        return workOrderMapper.toResponse(workOrderRepository.save(order));
    }

    @Override
    public WorkOrderResponse cancel(Long id, Long clientId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireClientOwner(order, clientId);

        if (order.getStatus() != WorkOrderStatus.PENDING
                && order.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Solo se pueden cancelar órdenes en estado PENDING o IN_PROGRESS");
        }

        order.setStatus(WorkOrderStatus.CANCELED);
        return workOrderMapper.toResponse(workOrderRepository.save(order));
    }

    private void requireExpertOwner(WorkOrder order, Long expertId) {
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            throw new IllegalStateException("Solo cuentas de tipo EXPERT pueden gestionar órdenes de trabajo");
        }

        var serviceExpert = order.getQuote().getOfferedService().getExpert();
        if (serviceExpert == null || !expertId.equals(serviceExpert.getIdUser())) {
            throw new IllegalStateException("Solo el experto propietario del servicio puede gestionar la orden");
        }
    }

    private void requireClientOwner(WorkOrder order, Long clientId) {
        var client = accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));

        if (client.getRole() != Role.CLIENT) {
            throw new IllegalStateException("Solo cuentas de tipo CLIENT pueden gestionar órdenes de trabajo");
        }

        if (order.getQuote().getClient() == null || !clientId.equals(order.getQuote().getClient().getIdUser())) {
            throw new IllegalStateException("Solo el cliente propietario de la orden puede gestionarla");
        }
    }

    @Override
    public Optional<WorkOrderResponse> findByQuoteId(Long quoteId) {
        return workOrderRepository.findByQuote_Id(quoteId)
                .map(workOrderMapper::toResponse);
    }

    @Override
    public List<WorkOrderResponse> findByClientId(Long clientId) {
        return workOrderRepository.findByQuote_Client_IdUser(clientId).stream()
                .map(workOrderMapper::toResponse)
                .toList();
    }

    @Override
    public List<WorkOrderResponse> findByExpertId(Long expertId) {
        return workOrderRepository.findByQuote_OfferedService_Expert_IdUser(expertId).stream()
                .map(workOrderMapper::toResponse)
                .toList();
    }

    @Override
    public List<WorkOrderResponse> findByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatus(status).stream()
                .map(workOrderMapper::toResponse)
                .toList();
    }
}
