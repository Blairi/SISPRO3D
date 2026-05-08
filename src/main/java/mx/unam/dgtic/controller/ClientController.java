package mx.unam.dgtic.controller;

import mx.unam.dgtic.domain.UserType;
import mx.unam.dgtic.dto.AccountDTO;
import mx.unam.dgtic.dto.ClientDTO;
import mx.unam.dgtic.service.ClientService;
import mx.unam.dgtic.service.impl.ClientServiceImpl;

public class ClientController {
    private final ClientService clientService;

    public ClientController() {
        this.clientService = new ClientServiceImpl();
    }

    public void createNewClient(String name, String lastName,
                           String email, String phone,
                           String password, String type) {
        AccountDTO accountDTO = AccountDTO.builder()
                .name(name)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .password(password)
                .type(UserType.valueOf(type))
                .build();
        ClientDTO clientDTO = ClientDTO.builder()
                        .account(accountDTO)
                                .build();
        clientService.create(clientDTO);
    }
}
