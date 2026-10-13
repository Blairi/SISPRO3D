package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.security.CurrentUser;
import com.sispro3d.unam.user.domain.Role;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Solicitud de cotización desde el catálogo. La ruta está autenticada en
 * {@code SecurityConfig}, así que un visitante anónimo es enviado a /login y
 * vuelve aquí gracias al request cache. El cliente se toma de la sesión.
 */
@Controller
@RequestMapping("/services")
public class QuoteRequestController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/{id}/quote")
    public String showForm(@PathVariable Long id, Model model) {
        requireClientRole();

        OfferedServiceResponse service = findApprovedService(id);

        model.addAttribute("service", service);
        model.addAttribute("quote", new QuoteRequest());
        return "services/quote-request";
    }

    @PostMapping("/{id}/quote")
    public String requestQuote(@PathVariable Long id,
                               @Valid @ModelAttribute("quote") QuoteRequest request,
                               BindingResult result,
                               Model model) {
        requireClientRole();

        OfferedServiceResponse service = findApprovedService(id);

        if (result.hasErrors()) {
            model.addAttribute("service", service);
            return "services/quote-request";
        }

        // El cliente y el servicio los fija el servidor, nunca el formulario.
        request.setClientId(currentUser.id());
        request.setOfferedServiceId(service.getId());

        quoteService.create(request);
        return "redirect:/client/quotes";
    }

    private OfferedServiceResponse findApprovedService(Long id) {
        return offeredServiceService.findById(id)
                .filter(svc -> svc.getStatus() == ServiceStatus.APPROVED)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));
    }

    private void requireClientRole() {
        if (currentUser.role() != Role.CLIENT) {
            throw new AccessDeniedException("Solo los clientes pueden solicitar cotizaciones");
        }
    }
}