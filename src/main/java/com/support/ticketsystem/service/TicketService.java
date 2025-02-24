package com.support.ticketsystem.service;

import com.support.ticketsystem.model.AuditLogEntry;
import com.support.ticketsystem.model.Ticket;
import com.support.ticketsystem.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    private List<AuditLogEntry> auditLogs = new ArrayList<>(); // Stocker les logs d'audit en mémoire


    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<AuditLogEntry> getAuditLogs() {
        return auditLogs;
    }

    public Ticket updateTicketStatus(Long id, Ticket updatedTicket) { //Utiliser Long au lieu de String pour l'ID
        Optional<Ticket> optionalTicket = ticketRepository.findById(id);

        if (optionalTicket.isPresent()) {
            Ticket existingTicket = optionalTicket.get();
            String oldStatus = existingTicket.getStatus(); //Récupérer l'ancien statut
            String newStatus = updatedTicket.getStatus(); //Récupérer le nouveau statut

            existingTicket.setStatus(newStatus); // Mettre à jour le statut

            // Ajouter une entrée dans le journal d'audit
            AuditLogEntry logEntry = new AuditLogEntry(id, oldStatus, newStatus, "System"); // Remplace "System" par un utilisateur réel si nécessaire
            auditLogs.add(logEntry);

            return ticketRepository.save(existingTicket); // Sauvegarder le ticket mis à jour
        }
        return null; // Retourner null si le ticket n'est pas trouvé
    }
}
