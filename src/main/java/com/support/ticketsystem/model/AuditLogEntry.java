package com.support.ticketsystem.model;

import java.time.LocalDateTime;

public class AuditLogEntry {
    private Long ticketId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime timestamp;
    private String user; // Qui a modifié le ticket

    public AuditLogEntry(Long ticketId, String oldStatus, String newStatus, String user) {
        this.ticketId = ticketId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
        this.user = user;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Ticket #" + ticketId + " : " + oldStatus + " → " + newStatus + " (par " + user + " à " + timestamp + ")";
    }
}