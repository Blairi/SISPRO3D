package com.sispro3d.unam.workorder.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ConflictException;
import com.sispro3d.unam.core.exception.ForbiddenException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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
            log.warn("Intento de crear orden por cuenta no cliente: id={}", request.getClientId());
            throw new ForbiddenException("Solo cuentas de tipo CLIENT pueden crear órdenes de trabajo");
        }

        var quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", request.getQuoteId()));

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            log.warn("Intento de crear orden desde cotización no ACCEPTED: id={}, estado={}", request.getQuoteId(), quote.getStatus());
            throw new BadRequestException("Solo se pueden crear órdenes a partir de cotizaciones en estado ACCEPTED");
        }
        if (quote.getClient() == null || !request.getClientId().equals(quote.getClient().getIdUser())) {
            log.warn("Intento de crear orden por cliente no propietario: cotización={}, cliente={}", request.getQuoteId(), request.getClientId());
            throw new ForbiddenException("Solo el cliente propietario de la cotización puede crear la orden");
        }

        requireQuoteSinOrden(quote.getId());

        WorkOrder order = workOrderMapper.toEntity(request);
        order.setQuote(quote);
        order.setStatus(WorkOrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(order);
        log.info("Orden de trabajo creada: id={}, cotización={}, cliente={}", saved.getId(), request.getQuoteId(), request.getClientId());
        return workOrderMapper.toResponse(saved);
    }

    private void requireQuoteSinOrden(Long quoteId) {
        if (workOrderRepository.existsByQuote_Id(quoteId)) {
            throw new ConflictException("La cotización " + quoteId + " ya tiene una orden de trabajo asociada");
        }
    }

    @Override
    public WorkOrderResponse update(Long id, WorkOrderRequest request) {
        WorkOrder existing = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        if (!accountRepository.existsById(request.getClientId())) {
            throw ResourceNotFoundException.forId("Account (client)", request.getClientId());
        }

        var quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", request.getQuoteId()));

        // update revalida las mismas reglas que create.
        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            log.warn("Intento de asociar orden a cotización no ACCEPTED: id={}, estado={}", request.getQuoteId(), quote.getStatus());
            throw new BadRequestException("Solo se pueden asociar órdenes a cotizaciones en estado ACCEPTED");
        }
        if (quote.getClient() == null || !request.getClientId().equals(quote.getClient().getIdUser())) {
            log.warn("Intento de asociar orden a cliente ajeno: orden={}, cliente={}", id, request.getClientId());
            throw new BadRequestException("La orden debe pertenecer al cliente de la cotización");
        }
        if (!quote.getId().equals(existing.getQuote().getId())) {
            requireQuoteSinOrden(quote.getId());
        }

        existing.setQuote(quote);
        WorkOrder updated = workOrderRepository.save(existing);
        log.info("Orden de trabajo actualizada: id={}", updated.getId());
        return workOrderMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!workOrderRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("WorkOrder", id);
        }
        workOrderRepository.deleteById(id);
        log.info("Orden de trabajo eliminada: id={}", id);
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
            log.warn("Intento de iniciar orden no PENDING: id={}, estado={}", id, order.getStatus());
            throw new BadRequestException("Solo se pueden iniciar órdenes en estado PENDING");
        }

        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        order.setStartedAt(LocalDateTime.now());
        WorkOrder updated = workOrderRepository.save(order);
        log.info("Orden de trabajo IN_PROGRESS: id={}, experto={}", updated.getId(), expertId);
        return workOrderMapper.toResponse(updated);
    }

    @Override
    public WorkOrderResponse markInReview(Long id, Long expertId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireExpertOwner(order, expertId);

        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            log.warn("Intento de enviar a revisión orden no IN_PROGRESS: id={}, estado={}", id, order.getStatus());
            throw new BadRequestException("Solo se pueden enviar a revisión órdenes en estado IN_PROGRESS");
        }

        order.setStatus(WorkOrderStatus.IN_REVIEW);
        WorkOrder updated = workOrderRepository.save(order);
        log.info("Orden de trabajo IN_REVIEW: id={}, experto={}", updated.getId(), expertId);
        return workOrderMapper.toResponse(updated);
    }

    @Override
    public WorkOrderResponse requestChanges(Long id, Long clientId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireClientOwner(order, clientId);

        if (order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            log.warn("Intento de solicitar cambios en orden no IN_REVIEW: id={}, estado={}", id, order.getStatus());
            throw new BadRequestException("Solo se pueden solicitar cambios en órdenes en estado IN_REVIEW");
        }

        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        WorkOrder updated = workOrderRepository.save(order);
        log.info("Orden de trabajo con cambios solicitados (IN_PROGRESS): id={}, cliente={}", updated.getId(), clientId);
        return workOrderMapper.toResponse(updated);
    }

    @Override
    public WorkOrderResponse complete(Long id, Long clientId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireClientOwner(order, clientId);

        if (order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            log.warn("Intento de completar orden no IN_REVIEW: id={}, estado={}", id, order.getStatus());
            throw new BadRequestException("Solo se pueden completar órdenes en estado IN_REVIEW");
        }

        order.setStatus(WorkOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        WorkOrder updated = workOrderRepository.save(order);
        log.info("Orden de trabajo COMPLETED: id={}, cliente={}", updated.getId(), clientId);
        return workOrderMapper.toResponse(updated);
    }

    @Override
    public WorkOrderResponse cancel(Long id, Long clientId) {
        WorkOrder order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        requireClientOwner(order, clientId);

        if (order.getStatus() != WorkOrderStatus.PENDING
                && order.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            log.warn("Intento de cancelar orden no cancelable: id={}, estado={}", id, order.getStatus());
            throw new BadRequestException("Solo se pueden cancelar órdenes en estado PENDING o IN_PROGRESS");
        }

        order.setStatus(WorkOrderStatus.CANCELED);
        WorkOrder updated = workOrderRepository.save(order);
        log.info("Orden de trabajo CANCELED: id={}, cliente={}", updated.getId(), clientId);
        return workOrderMapper.toResponse(updated);
    }

    private void requireExpertOwner(WorkOrder order, Long expertId) {
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            log.warn("Intento de gestionar orden por cuenta no experta: id={}", expertId);
            throw new ForbiddenException("Solo cuentas de tipo EXPERT pueden gestionar órdenes de trabajo");
        }

        var serviceExpert = order.getQuote().getOfferedService().getExpert();
        if (serviceExpert == null || !expertId.equals(serviceExpert.getIdUser())) {
            log.warn("Intento de gestionar orden por experto no propietario: orden={}, experto={}", order.getId(), expertId);
            throw new ForbiddenException("Solo el experto propietario del servicio puede gestionar la orden");
        }
    }

    private void requireClientOwner(WorkOrder order, Long clientId) {
        var client = accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));

        if (client.getRole() != Role.CLIENT) {
            log.warn("Intento de gestionar orden por cuenta no cliente: id={}", clientId);
            throw new ForbiddenException("Solo cuentas de tipo CLIENT pueden gestionar órdenes de trabajo");
        }

        if (order.getQuote().getClient() == null || !clientId.equals(order.getQuote().getClient().getIdUser())) {
            log.warn("Intento de gestionar orden por cliente no propietario: orden={}, cliente={}", order.getId(), clientId);
            throw new ForbiddenException("Solo el cliente propietario de la orden puede gestionarla");
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
