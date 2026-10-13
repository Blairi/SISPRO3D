package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista de cotizaciones del cliente. Solo lista y opera sobre las suyas: el
 * servicio de dominio ya rechaza cualquier cotización de otro cliente, y el
 * refusal se muestra como mensaje en lugar de una traza.
 */
@Controller
@RequestMapping("/client/quotes")
public class ClientQuotesController {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping
    public String list(Model model) {
        Long userId = currentUser.id();

        List<QuoteResponse> quotes = quoteService.findByClientId(userId);

        Map<Long, String> titles = new HashMap<>();
        for (QuoteResponse quote : quotes) {
            titles.computeIfAbsent(quote.getOfferedServiceId(), id ->
                    offeredServiceService.findById(id)
                            .map(OfferedServiceResponse::getTitle)
                            .orElse("Servicio"));
        }

        model.addAttribute("pendingQuotes", quotes.stream()
                .filter(q -> q.getStatus() == QuoteStatus.PENDING).toList());
        model.addAttribute("answeredQuotes", quotes.stream()
                .filter(q -> q.getStatus() != QuoteStatus.PENDING).toList());
        model.addAttribute("serviceTitles", titles);
        return "client/quotes";
    }

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();

        try {
            var acceptance = quoteService.accept(id, userId);
            attributes.addFlashAttribute("success",
                    "Cotización aceptada. Se creó la orden de trabajo #"
                            + acceptance.workOrder().getId() + ".");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/client/quotes";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();

        try {
            quoteService.reject(id, userId);
            attributes.addFlashAttribute("success", "Cotización rechazada.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/client/quotes";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();

        try {
            quoteService.cancel(id, userId);
            attributes.addFlashAttribute("success", "Solicitud cancelada.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/client/quotes";
    }
}