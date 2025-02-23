package com.support.ticketsystem.supportITSW;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.support.ticketsystem.model.Ticket;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


public class mainTicket extends JFrame {
    // UI Components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> priorityComboBox;
    private JComboBox<String> categoryComboBox;
    private JLabel creationDateLabel;
    private JButton submitButton;

    public mainTicket() {
        initComponents();
    }

    public void initComponents() {
        setTitle("Support Ticket System");
        setSize(600, 400);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set Application Icon
        setAppIcon();

        // Title Label (Top of Form)
        JLabel formTitle = new JLabel("Create a New Support Ticket", JLabel.CENTER);
        formTitle.setFont(new Font("Arial", Font.BOLD, 18)); // Set font size & bold

        JLabel titleLabel = new JLabel("Title:");
        titleField = new JTextField(20);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        JLabel priorityLabel = new JLabel("Priority:");
        String[] priorities = {"Low", "Medium", "High"};
        priorityComboBox = new JComboBox<>(priorities);

        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Network", "Hardware", "Software", "Other"};
        categoryComboBox = new JComboBox<>(categories);

        JLabel dateLabel = new JLabel("Creation Date:");
        creationDateLabel = new JLabel(getCurrentDate());

        submitButton = new JButton("Submit Ticket");

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Add Title at the Top
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(formTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = ++row; add(titleLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; add(descriptionLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; add(descriptionScroll, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; add(priorityLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; add(priorityComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; add(categoryLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; add(categoryComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; add(dateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; add(creationDateLabel, gbc);

        gbc.gridx = 1; gbc.gridy = ++row; add(submitButton, gbc);

        // Add button action listener
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitTicket();
            }
        });
    }

    // 🔹 Method to Load Application Icon
    private void setAppIcon() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/support-ticket.png"));
        if (icon.getImage() != null) {
            setIconImage(icon.getImage());
        } else {
            System.out.println("Icon not found!");
        }
    }

    private String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    private void submitTicket() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String priority = (String) priorityComboBox.getSelectedItem();
        String category = (String) categoryComboBox.getSelectedItem();
        String creationDate = creationDateLabel.getText();

        // Créer un objet Ticket
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setCreationDate(creationDate);

        // Création de RestTemplate avec authentification
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tickets";

        // Ajouter les headers avec Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin"); // Remplace par tes identifiants Spring Security
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Ticket> request = new HttpEntity<>(ticket, headers);

        // Envoyer la requête HTTP POST
        try {
            ResponseEntity<Ticket> response = restTemplate.exchange(url, HttpMethod.POST, request, Ticket.class);

            // Vérifier si la requête a réussi
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Ticket createdTicket = response.getBody();
                JOptionPane.showMessageDialog(this, "Ticket Created:\n" +
                        "Title: " + createdTicket.getTitle() + "\n" +
                        "Description: " + createdTicket.getDescription() + "\n" +
                        "Priority: " + createdTicket.getPriority() + "\n" +
                        "Category: " + createdTicket.getCategory() + "\n" +
                        "Date: " + createdTicket.getCreationDate(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error creating ticket. Server returned: " + response.getStatusCode(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Reset form fields
        titleField.setText("");
        descriptionArea.setText("");
        priorityComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new mainTicket().setVisible(true));
    }
}
