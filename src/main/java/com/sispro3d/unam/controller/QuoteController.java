package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.QuoteJdbcDAO;
import com.sispro3d.unam.dto.QuoteDTO;
import com.sispro3d.unam.service.QuoteService;
import com.sispro3d.unam.service.impl.QuoteServiceImpl;

import java.util.Optional;

public class QuoteController {
    private QuoteService quoteService;

    public QuoteController() {
        this.quoteService = new QuoteServiceImpl(new QuoteJdbcDAO());
    }

    public void displayQuote(int id) {
        System.out.println("Displaying quote with id = " + id);
        Optional<QuoteDTO> quoteDTO = quoteService.findById(id);
        System.out.println("quoteDTO = " + quoteDTO);
    }

    public void displayAllQuotes() {
        System.out.println("Displaying all quotes:");
        quoteService.findAll().forEach(System.out::println);
    }
}
