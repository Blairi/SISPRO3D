package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/services")
public class ServiceCatalogController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String index(Model model) {
        List<OfferedServiceResponse> services = offeredServiceService.findByStatus(ServiceStatus.APPROVED).stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
        model.addAttribute("services", services);
        return "services/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OfferedServiceResponse service = offeredServiceService.findById(id)
                .filter(svc -> svc.getStatus() == ServiceStatus.APPROVED)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        AccountResponse expert = accountService.findById(service.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", service.getExpertId()));

        model.addAttribute("service", service);
        model.addAttribute("expert", expert);
        return "services/detail";
    }
}