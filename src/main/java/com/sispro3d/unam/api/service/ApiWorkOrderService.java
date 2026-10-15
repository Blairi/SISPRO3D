package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.WorkOrderCreateDTO;
import com.sispro3d.unam.api.dto.WorkOrderResponseDTO;
import com.sispro3d.unam.api.mapper.ApiWorkOrderMapper;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.security.ApiCurrentUser;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiWorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderService workOrderService;
    private final ApiWorkOrderMapper mapper;
    private final AccountRepository accountRepository;
    private final ApiOrderAccess access;

    public ApiWorkOrderService(WorkOrderRepository workOrderRepository,
                               WorkOrderService workOrderService,
                               ApiWorkOrderMapper mapper,
                               AccountRepository accountRepository,
                               ApiOrderAccess access) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderService = workOrderService;
        this.mapper = mapper;
        this.accountRepository = accountRepository;
        this.access = access;
    }

    public WorkOrderResponseDTO findById(Long id) {
        var order = findOrder(id);
        access.requireParticipant(order);
        return mapper.toResponse(order);
    }

    /**
     * Crea la orden delegando en el dominio, que exige que la cotización esté
     * ACCEPTED y que no tenga ya una orden. El cliente es la cuenta del token.
     */
    public WorkOrderResponseDTO create(WorkOrderCreateDTO request) {
        Long clientId = access.currentAccountId();

        workOrderService.create(WorkOrderRequest.builder()
                .clientId(clientId)
                .quoteId(request.getQuoteId())
                .build());

        return workOrderRepository.findByQuote_Id(request.getQuoteId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalStateException("The work order could not be created"));
    }

    /** Listado acotado a la identidad del token de acceso. */
    public List<WorkOrderResponseDTO> findAll() {
        var actor = requireActor();
        return ordersVisibleTo(actor).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<WorkOrderResponseDTO> findByClient(Long clientId) {
        var actor = requireActor();
        if (actor.getRole() == Role.CLIENT && !actor.getIdUser().equals(clientId)) {
            throw new ForbiddenException("A client can only list their own work orders");
        }
        return workOrderRepository.findByQuote_Client_IdUser(clientId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<WorkOrderResponseDTO> findByExpert(Long expertId) {
        var actor = requireActor();
        if (actor.getRole() == Role.EXPERT && !actor.getIdUser().equals(expertId)) {
            throw new ForbiddenException("An expert can only list their own work orders");
        }
        return workOrderRepository.findByQuote_OfferedService_Expert_IdUser(expertId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<WorkOrderResponseDTO> findByStatus(WorkOrderStatus status) {
        var actor = requireActor();
        return ordersVisibleTo(actor).stream()
                .filter(order -> order.getStatus() == status)
                .map(mapper::toResponse)
                .toList();
    }

    private List<WorkOrder> ordersVisibleTo(Account actor) {
        return switch (actor.getRole()) {
            case CLIENT -> workOrderRepository.findByQuote_Client_IdUser(actor.getIdUser());
            case EXPERT -> workOrderRepository.findByQuote_OfferedService_Expert_IdUser(actor.getIdUser());
            default -> workOrderRepository.findAll();
        };
    }

    private Account requireActor() {
        Long actorId = access.currentAccountId();
        return accountRepository.findById(actorId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", actorId));
    }

    /**
     * Delega la transición al servicio de dominio, que aplica la máquina de
     * estados junto con la verificación del actor. El actor es la cuenta del
     * token de acceso, nunca un id enviado en el cuerpo.
     */
    public WorkOrderResponseDTO updateStatus(Long id, WorkOrderStatus newStatus) {
        Long actorId = ApiCurrentUser.requireAccountId();

        var order = findOrder(id);

        switch (newStatus) {
            case PENDING -> throw new BadRequestException("Una orden no puede volver al estado PENDING");
            case IN_PROGRESS -> {
                if (order.getStatus() == WorkOrderStatus.IN_REVIEW) {
                    workOrderService.requestChanges(id, actorId);
                } else {
                    workOrderService.start(id, actorId);
                }
            }
            case IN_REVIEW -> workOrderService.markInReview(id, actorId);
            case COMPLETED -> workOrderService.complete(id, actorId);
            case CANCELED -> workOrderService.cancel(id, actorId);
        }

        return workOrderRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));
    }

    private WorkOrder findOrder(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));
    }
}
