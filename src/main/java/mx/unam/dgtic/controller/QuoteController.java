package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.QuoteJdbcDAO;
import mx.unam.dgtic.dto.QuoteDTO;
import mx.unam.dgtic.service.QuoteService;
import mx.unam.dgtic.service.impl.QuoteServiceImpl;

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
