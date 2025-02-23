package com.support.ticketsystem;

import com.support.ticketsystem.supportITSW.mainTicket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class ItSupportTicketApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false"); // Désactiver le mode headless
		//System.out.println("Is headless: " + java.awt.GraphicsEnvironment.isHeadless());
		SpringApplication.run(ItSupportTicketApplication.class, args);
		// Lancer l'interface Swing sur le thread de l'interface graphique
		SwingUtilities.invokeLater(() -> new mainTicket().setVisible(true));
	}
}