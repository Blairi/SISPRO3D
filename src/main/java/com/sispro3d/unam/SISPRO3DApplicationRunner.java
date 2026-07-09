package com.sispro3d.unam;

import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SISPRO3DApplicationRunner implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DeliverableService deliverableService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("===== SISPRO3D & Spring Boot =====");
        System.out.println(deliverableService.findAll());
    }
}
