package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.dto.ServiceSearchCriteria;
import com.sispro3d.unam.offeredservice.dto.ServiceSortOrder;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private static final int FEATURED_SERVICES_LIMIT = 6;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        List<OfferedServiceResponse> featured = offeredServiceService.search(
                new ServiceSearchCriteria(null, null, null, null, null, ServiceSortOrder.NEWEST));
        if (featured.size() > FEATURED_SERVICES_LIMIT) {
            featured = featured.subList(0, FEATURED_SERVICES_LIMIT);
        }
        model.addAttribute("featuredServices", featured);
        model.addAttribute("categories", categoryService.findAll());
        return "home";
    }
}
