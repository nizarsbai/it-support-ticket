package com.support.ticketsystem;

import com.support.ticketsystem.supportITSW.mainTicket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class ItSupportTicketApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false"); // Disable headless mode

		// Start the Spring application and get the application context
		SpringApplication.run(ItSupportTicketApplication.class, args);

		// Retrieve the AuditLogService bean from Spring's context
		//AuditLogService auditLogService = context.getBean(AuditLogService.class);

		// Start the Swing UI on the Event Dispatch Thread
		SwingUtilities.invokeLater(() -> new mainTicket().setVisible(true));
	}
}