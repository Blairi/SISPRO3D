package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.dto.ClientResponse;
import com.sispro3d.unam.user.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    public void displayClient(long id) {
        System.out.println("Displaying client with id = " + id);
        Optional<ClientResponse> clientDTO = clientService.findById(id);
        System.out.println("clientDTO = " + clientDTO);
    }

    public void displayAllClients() {
        System.out.println("Displaying all clients:");
        List<ClientResponse> clients = clientService.findAll();
        clients.forEach(System.out::println);
    }
}
