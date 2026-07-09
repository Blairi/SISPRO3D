package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.dto.AdminResponse;
import com.sispro3d.unam.user.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    public void displayAdmin(long id) {
        System.out.println("Displaying admin with id = " + id);
        Optional<AdminResponse> adminDTO = adminService.findById(id);
        System.out.println("adminDTO = " + adminDTO);
    }

    public void displayAllAdmins() {
        System.out.println("Displaying all admins:");
        List<AdminResponse> admins = adminService.findAll();
        admins.forEach(System.out::println);
    }
}
