package com.support.ticketsystem.controller;

import com.support.ticketsystem.model.Ticket;
import com.support.ticketsystem.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.saveTicket(ticket);
    }

    @GetMapping
    public List<Ticket> getTickets() {
        return ticketService.getAllTickets();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicketStatus(@PathVariable String id, @RequestBody Ticket ticket) {
        // Find the ticket by ID and update the status
        Ticket updatedTicket = ticketService.updateTicketStatus(id, ticket);

        if (updatedTicket != null) {
            return ResponseEntity.ok(updatedTicket); // Return updated ticket as a response
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Ticket not found
        }
    }
}
