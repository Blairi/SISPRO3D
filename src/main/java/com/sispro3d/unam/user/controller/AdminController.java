package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.dao.AdminJdbcDAO;
import com.sispro3d.unam.user.dto.AdminDTO;
import com.sispro3d.unam.user.service.AdminService;
import com.sispro3d.unam.user.service.impl.AdminServiceImpl;

import java.util.Optional;

public class AdminController {
    private AdminService adminService;

    public AdminController() {
        this.adminService = new AdminServiceImpl(new AdminJdbcDAO());
    }

    public void displayAdmin(int id) {
        System.out.println("Displaying admin with id = " + id);
        Optional<AdminDTO> adminDTO = adminService.findById(id);
        System.out.println("adminDTO = " + adminDTO);
    }

    public void displayAllAdmins() {
        System.out.println("Displaying all admins:");
        adminService.findAll().forEach(System.out::println);
    }
}
