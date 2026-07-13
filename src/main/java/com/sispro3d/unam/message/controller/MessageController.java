package com.sispro3d.unam.message.controller;

import com.sispro3d.unam.message.dto.MessageResponse;
import com.sispro3d.unam.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    public void displayMessage(long id) {
        System.out.println("Displaying message with id = " + id);
        Optional<MessageResponse> message = messageService.findById(id);
        System.out.println("message = " + message);
    }

    public void displayAllMessages() {
        System.out.println("Displaying all messages:");
        List<MessageResponse> messages = messageService.findAll();
        messages.forEach(System.out::println);
    }
}
