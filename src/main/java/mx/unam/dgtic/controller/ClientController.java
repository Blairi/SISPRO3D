package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.ClientJdbcDAO;
import mx.unam.dgtic.dto.ClientDTO;
import mx.unam.dgtic.service.ClientService;
import mx.unam.dgtic.service.impl.ClientServiceImpl;

import java.util.Optional;

public class ClientController {
    private ClientService clientService;

    public ClientController() {
        this.clientService = new ClientServiceImpl(new ClientJdbcDAO());
    }

    public void displayClient(int id) {
        System.out.println("Displaying client with id = " + id);
        Optional<ClientDTO> clientDTO = clientService.findById(id);
        System.out.println("clientDTO = " + clientDTO);
    }

    public void displayAllClients() {
        System.out.println("Displaying all clients:");
        clientService.findAll().forEach(System.out::println);
    }
}
