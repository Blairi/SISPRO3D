package com.sispro3d.unam.quote.controller;

import com.sispro3d.unam.quote.dao.QuoteJdbcDAO;
import com.sispro3d.unam.quote.dto.QuoteDTO;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.quote.service.impl.QuoteServiceImpl;

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
