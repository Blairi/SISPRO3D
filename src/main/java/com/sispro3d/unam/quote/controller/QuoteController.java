package com.sispro3d.unam.quote.controller;

import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    public void displayQuote(long id) {
        System.out.println("Displaying quote with id = " + id);
        Optional<QuoteResponse> quoteDTO = quoteService.findById(id);
        System.out.println("quoteDTO = " + quoteDTO);
    }

    public void displayAllQuotes() {
        System.out.println("Displaying all quotes:");
        List<QuoteResponse> quotes = quoteService.findAll();
        quotes.forEach(System.out::println);
    }
}
