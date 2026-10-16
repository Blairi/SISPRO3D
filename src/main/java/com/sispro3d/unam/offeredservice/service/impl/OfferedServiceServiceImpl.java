package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.dto.ServiceSearchCriteria;
import com.sispro3d.unam.offeredservice.dto.ServiceSortOrder;
import com.sispro3d.unam.offeredservice.mapper.OfferedServiceMapper;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class OfferedServiceServiceImpl implements OfferedServiceService {

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;
    @Autowired
    private OfferedServiceMapper offeredServiceMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<OfferedServiceResponse> findAll() {
        return offeredServiceRepository.findAll().stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<OfferedServiceResponse> findById(Long id) {
        return offeredServiceRepository.findById(id)
                .map(offeredServiceMapper::toResponse);
    }

    @Override
    public OfferedServiceResponse create(OfferedServiceRequest request) {
        var expert = accountRepository.findById(request.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", request.getExpertId()));

        if (expert.getRole() != Role.EXPERT) {
            log.warn("Intento de publicar servicio por cuenta no experta: id={}", expert.getIdUser());
            throw new ForbiddenException("Solo cuentas de tipo EXPERT pueden publicar servicios");
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }

        OfferedService service = offeredServiceMapper.toEntity(request);
        service.setExpert(expert);
        service.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        service.setStatus(ServiceStatus.PENDING);

        OfferedService saved = offeredServiceRepository.save(service);
        log.info("Servicio ofrecido creado: id={}, experto={}", saved.getId(), expert.getIdUser());
        return offeredServiceMapper.toResponse(saved);
    }

    @Override
    public OfferedServiceResponse update(Long id, OfferedServiceRequest request) {
        OfferedService existing = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        if (!accountRepository.existsById(request.getExpertId())) {
            throw ResourceNotFoundException.forId("Account (expert)", request.getExpertId());
        }
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }

        offeredServiceMapper.updateEntityFromRequest(request, existing);
        existing.setExpert(accountRepository.getReferenceById(request.getExpertId()));
        existing.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        existing.setUpdatedAt(LocalDateTime.now());
        OfferedService updated = offeredServiceRepository.save(existing);
        log.info("Servicio ofrecido actualizado: id={}", updated.getId());
        return offeredServiceMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!offeredServiceRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("OfferedService", id);
        }
        offeredServiceRepository.deleteById(id);
        log.info("Servicio ofrecido eliminado: id={}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return offeredServiceRepository.existsById(id);
    }

    @Override
    public OfferedServiceResponse approve(Long id, Long adminId) {
        var service = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        var admin = accountRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (admin)", adminId));

        if (admin.getRole() != Role.ADMIN) {
            log.warn("Intento de aprobar servicio por cuenta no admin: id={}", adminId);
            throw new ForbiddenException("Solo cuentas de tipo ADMIN pueden aprobar servicios");
        }

        if (service.getStatus() != ServiceStatus.PENDING) {
            log.warn("Intento de aprobar servicio no PENDING: id={}, estado={}", id, service.getStatus());
            throw new BadRequestException("Solo se pueden aprobar servicios en estado PENDING");
        }

        service.setStatus(ServiceStatus.APPROVED);
        service.setAdmin(admin);

        OfferedService updated = offeredServiceRepository.save(service);
        log.info("Servicio ofrecido aprobado: id={}, admin={}", updated.getId(), adminId);
        return offeredServiceMapper.toResponse(updated);
    }

    // TODO: agregar un mensaje con la razón del rechazo
    @Override
    public OfferedServiceResponse reject(Long id, Long adminId) {
        var service = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        var admin = accountRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (admin)", adminId));

        if (admin.getRole() != Role.ADMIN) {
            log.warn("Intento de rechazar servicio por cuenta no admin: id={}", adminId);
            throw new ForbiddenException("Solo cuentas de tipo ADMIN pueden aprobar servicios");
        }

        if (service.getStatus() != ServiceStatus.PENDING) {
            log.warn("Intento de rechazar servicio no PENDING: id={}, estado={}", id, service.getStatus());
            throw new BadRequestException("Solo se pueden aprobar servicios en estado PENDING");
        }

        service.setStatus(ServiceStatus.REJECTED);
        service.setAdmin(admin);

        OfferedService updated = offeredServiceRepository.save(service);
        log.info("Servicio ofrecido rechazado: id={}, admin={}", updated.getId(), adminId);
        return offeredServiceMapper.toResponse(updated);
    }

    @Override
    public OfferedServiceResponse markPending(Long id, Long adminId) {
        var service = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        var admin = accountRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (admin)", adminId));

        if (admin.getRole() != Role.ADMIN) {
            log.warn("Intento de modificar estado de servicio por cuenta no admin: id={}", adminId);
            throw new ForbiddenException("Solo cuentas de tipo ADMIN pueden modificar el estado de un servicio");
        }

        service.setStatus(ServiceStatus.PENDING);
        service.setAdmin(admin);

        OfferedService updated = offeredServiceRepository.save(service);
        log.info("Servicio ofrecido marcado como PENDING: id={}, admin={}", updated.getId(), adminId);
        return offeredServiceMapper.toResponse(updated);
    }

    @Override
    public List<OfferedServiceResponse> findByExpertId(Long expertId) {
        return offeredServiceRepository.findByExpert_IdUser(expertId).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<OfferedServiceResponse> findByCategoryId(Long categoryId) {
        return offeredServiceRepository.findByCategory_Id(categoryId).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<OfferedServiceResponse> findByStatus(ServiceStatus status) {
        return offeredServiceRepository.findByStatus(status).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<OfferedServiceResponse> search(ServiceSearchCriteria criteria) {
        String query = criteria.query() == null ? "" : criteria.query().trim().toLowerCase(Locale.ROOT);
        Long categoryId = criteria.categoryId();
        BigDecimal minPrice = criteria.minPrice();
        BigDecimal maxPrice = criteria.maxPrice();
        Integer maxDeliveryDays = criteria.maxDeliveryDays();

        List<OfferedServiceResponse> results = findByStatus(ServiceStatus.APPROVED).stream()
                .filter(service -> query.isEmpty() || matchesQuery(service, query))
                .filter(service -> categoryId == null || categoryId.equals(service.getCategoryId()))
                .filter(service -> minPrice == null
                        || (service.getBasePrice() != null && service.getBasePrice().compareTo(minPrice) >= 0))
                .filter(service -> maxPrice == null
                        || (service.getBasePrice() != null && service.getBasePrice().compareTo(maxPrice) <= 0))
                .filter(service -> maxDeliveryDays == null
                        || (service.getDeliveryTimeDays() != null && service.getDeliveryTimeDays() <= maxDeliveryDays))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        results.sort(comparatorFor(criteria.sortOrNewest()));
        return results;
    }

    private boolean matchesQuery(OfferedServiceResponse service, String lowerQuery) {
        return containsIgnoreCase(service.getTitle(), lowerQuery)
                || containsIgnoreCase(service.getDescription(), lowerQuery)
                || containsIgnoreCase(service.getExpertName(), lowerQuery);
    }

    private boolean containsIgnoreCase(String value, String lowerQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerQuery);
    }

    private Comparator<OfferedServiceResponse> comparatorFor(ServiceSortOrder sort) {
        return switch (sort) {
            case PRICE_ASC -> (a, b) -> comparePrice(a, b, true);
            case PRICE_DESC -> (a, b) -> comparePrice(a, b, false);
            case NEWEST -> (a, b) -> {
                LocalDateTime first = a.getCreatedAt();
                LocalDateTime second = b.getCreatedAt();
                if (first == null) return second == null ? 0 : 1;
                if (second == null) return -1;
                return second.compareTo(first);
            };
        };
    }

    private int comparePrice(OfferedServiceResponse a, OfferedServiceResponse b, boolean ascending) {
        BigDecimal first = a.getBasePrice();
        BigDecimal second = b.getBasePrice();
        if (first == null) return second == null ? 0 : 1;
        if (second == null) return -1;
        return ascending ? first.compareTo(second) : second.compareTo(first);
    }
}
