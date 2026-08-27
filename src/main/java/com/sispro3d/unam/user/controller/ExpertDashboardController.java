package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.dto.AccountResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/expert")
public class ExpertDashboardController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        var services = offeredServiceService.findByExpertId(userId);
        model.addAttribute("services", services);
        return "expert/dashboard";
    }

    @GetMapping("/services/new")
    public String showCreateForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("offeredservice", new OfferedServiceRequest());
        model.addAttribute("categories", categoryRepository.findAll());
        return "expert/create-service";
    }

    @PostMapping("/services/new")
    public String createService(
            @ModelAttribute OfferedServiceRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        request.setExpertId(userId);
        offeredServiceService.create(request);
        return "redirect:/expert/dashboard";
    }
}
