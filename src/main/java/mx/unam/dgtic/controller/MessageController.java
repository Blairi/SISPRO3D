package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.MessageJdbcDAO;
import mx.unam.dgtic.dto.MessageDTO;
import mx.unam.dgtic.service.MessageService;
import mx.unam.dgtic.service.impl.MessageServiceImpl;

import java.util.Optional;

public class MessageController {
    private MessageService messageService;

    public MessageController() {
        this.messageService = new MessageServiceImpl(new MessageJdbcDAO());
    }

    public void displayMessage(int id) {
        System.out.println("Displaying message with id = " + id);
        Optional<MessageDTO> messageDTO = messageService.findById(id);
        System.out.println("messageDTO = " + messageDTO);
    }

    public void displayAllMessages() {
        System.out.println("Displaying all messages:");
        messageService.findAll().forEach(System.out::println);
    }
}
