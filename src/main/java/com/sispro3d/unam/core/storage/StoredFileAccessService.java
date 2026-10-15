package com.sispro3d.unam.core.storage;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Autoriza la lectura de un archivo almacenado: resuelve la referencia a su
 * entregable o previsualización para encontrar la orden propietaria y solo
 * permite el acceso al cliente o al experto de esa orden.
 */
@Service
public class StoredFileAccessService {

    private final DeliverableRepository deliverableRepository;
    private final PreviewRepository previewRepository;
    private final FileStorageService fileStorageService;

    public StoredFileAccessService(DeliverableRepository deliverableRepository,
                                   PreviewRepository previewRepository,
                                   FileStorageService fileStorageService) {
        this.deliverableRepository = deliverableRepository;
        this.previewRepository = previewRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Optional<Path> resolveForUser(String reference, Long userId) {
        if (userId == null || !FileStorageService.isStoredFile(reference)) {
            return Optional.empty();
        }

        WorkOrder order = owningWorkOrder(reference);
        if (order == null || !isParticipant(order, userId)) {
            return Optional.empty();
        }

        try {
            Path path = fileStorageService.resolve(reference);
            return Files.isRegularFile(path) ? Optional.of(path) : Optional.empty();
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private WorkOrder owningWorkOrder(String reference) {
        Optional<Deliverable> deliverable = deliverableRepository.findFirstByUrlFile(reference);
        if (deliverable.isPresent()) {
            return deliverable.get().getWorkOrder();
        }
        return previewRepository.findFirstByUrlFile(reference)
                .map(Preview::getDeliverable)
                .map(Deliverable::getWorkOrder)
                .orElse(null);
    }

    private boolean isParticipant(WorkOrder order, Long userId) {
        Quote quote = order.getQuote();
        if (quote.getClient().getIdUser().equals(userId)) {
            return true;
        }
        return quote.getOfferedService().getExpert().getIdUser().equals(userId);
    }
}
