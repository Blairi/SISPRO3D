package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.MessageJdbcDAO;
import com.sispro3d.unam.dto.MessageDTO;
import com.sispro3d.unam.service.MessageService;
import com.sispro3d.unam.service.impl.MessageServiceImpl;

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
