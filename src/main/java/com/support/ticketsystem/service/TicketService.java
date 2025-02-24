package com.support.ticketsystem.service;

import com.support.ticketsystem.model.Ticket;
import com.support.ticketsystem.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket updateTicketStatus(String id, Ticket ticket) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(Long.valueOf(id)); // Find the ticket by ID

        if (optionalTicket.isPresent()) {
            Ticket existingTicket = optionalTicket.get();
            existingTicket.setStatus(ticket.getStatus()); // Update the status
            return ticketRepository.save(existingTicket); // Save the updated ticket
        }
        return null; // Return null if the ticket is not found
    }
}
