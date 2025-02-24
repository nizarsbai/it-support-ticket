package com.support.ticketsystem.model;

import java.time.LocalDateTime;

public class AuditLog {
    private Long ticketId;
    private String oldStatus;
    private String newStatus;
    private String comment;
    private String timestamp;

    public AuditLog(Long ticketId, String oldStatus, String newStatus, String comment, String timestamp) {
        this.ticketId = ticketId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.comment = comment;
        this.timestamp = timestamp;
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

    public String getComment() {
        return comment;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
