package com.support.ticketsystem.supportITSW;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.support.ticketsystem.model.Ticket;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class mainTicket extends JFrame {
    // UI Components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> priorityComboBox;
    private JComboBox<String> categoryComboBox;
    private JLabel creationDateLabel;
    private JButton submitButton;
    private JTabbedPane tabbedPane;
    private JPanel ticketCreationPanel;
    private JPanel statusTrackingPanel;
    private JTable statusTable;

    public mainTicket() {
        initComponents();
    }

    public void initComponents() {
        setTitle("Support Ticket System");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set Application Icon
        setAppIcon();

        // Create the Tabbed Pane
        tabbedPane = new JTabbedPane();

        // Create the Ticket Creation Panel
        ticketCreationPanel = createTicketCreationPanel();
        tabbedPane.addTab("Create Ticket", ticketCreationPanel);

        // Create the Status Tracking Panel
        statusTrackingPanel = createStatusTrackingPanel();
        tabbedPane.addTab("Status Tracking", statusTrackingPanel);

        // Add Tabbed Pane to the JFrame
        add(tabbedPane);

    }

    // Method to create the Ticket Creation Panel (unchanged)
    private JPanel createTicketCreationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

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
        panel.add(formTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = ++row; panel.add(titleLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; panel.add(descriptionLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(descriptionScroll, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; panel.add(priorityLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(priorityComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; panel.add(categoryLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(categoryComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; panel.add(dateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(creationDateLabel, gbc);

        gbc.gridx = 1; gbc.gridy = ++row; panel.add(submitButton, gbc);

        // Add button action listener
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitTicket();
            }
        });

        return panel;
    }

    // Method to create the Status Tracking Panel
    private JPanel createStatusTrackingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Table for displaying tickets
        String[] columnNames = {"Ticket ID", "Title", "Category", "Priority", "Status", "Creation Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        statusTable = new JTable(model); // Assign to class-level variable

        // Fetch tickets from the server and display in the table
        loadTickets(model);

        // Make the "Status" column editable with a combo box
        JComboBox<String> statusComboBox = new JComboBox<>(new String[] {"New", "In Progress", "Resolved"});
        statusTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusComboBox));

        // Add an editor listener to update the status when it changes
        statusTable.getColumnModel().getColumn(4).getCellEditor().addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                int row = statusTable.getSelectedRow();
                if (row == -1) return;  // No row selected, exit

                // Ensure all necessary values are fetched
                String newStatus = (String) statusTable.getValueAt(row, 4); // "Status" column is index 4
                Long ticketId = (Long) statusTable.getValueAt(row, 0); // "Ticket ID" column is index 0

                // Ensure the ticket ID and status are available before updating
                if (ticketId != null && newStatus != null) {
                    // Create a ticket with the updated status and send it to the server
                    Ticket ticket = new Ticket(ticketId,
                            (String) statusTable.getValueAt(row, 1), // Title
                            (String) statusTable.getValueAt(row, 2), // Description
                            (String) statusTable.getValueAt(row, 3), // Priority
                            (String) statusTable.getValueAt(row, 4), // Category
                            (String) statusTable.getValueAt(row, 5), // Creation Date
                            newStatus // Updated status
                    );
                    updateTicketStatus(ticket); // Make the update call
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                // Handle canceling the edit if needed
            }
        });

        JScrollPane scrollPane = new JScrollPane(statusTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


    private void updateTicketStatus(Ticket ticket) {
        // Ensure ticket ID is not null
        if (ticket.getId() == null) {
            JOptionPane.showMessageDialog(this, "Ticket ID is missing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tickets/" + ticket.getId(); // Assuming the URL includes ticket ID (as Long)

        // Add headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin"); // Replace with your Spring Security credentials
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Ticket> request = new HttpEntity<>(ticket, headers);

        try {
            // Print for debugging
            System.out.println("Updating ticket with ID: " + ticket.getId());
            System.out.println("URL: " + url);

            // Send PUT request to update the ticket
            ResponseEntity<Ticket> response = restTemplate.exchange(url, HttpMethod.PUT, request, Ticket.class);

            // Check if the request was successful
            if (response.getStatusCode().is2xxSuccessful()) {
                Ticket updatedTicket = response.getBody();
                JOptionPane.showMessageDialog(this, "Ticket status updated successfully:\n" +
                        "ID: " + updatedTicket.getId() + "\n" +
                        "New Status: " + updatedTicket.getStatus(), "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reload tickets to reflect the status change
                loadTickets((DefaultTableModel) statusTable.getModel());
            } else {
                JOptionPane.showMessageDialog(this, "Error updating ticket status. Server returned: " + response.getStatusCode(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HttpClientErrorException e) {
            JOptionPane.showMessageDialog(this, "HTTP Error: " + e.getStatusCode() + " - " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // Fetch tickets from the server to display in the Status Tracking tab
    private void loadTickets(DefaultTableModel model) {
        // RestTemplate setup
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tickets";

        // Headers setup (No Auth required as per your config)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make GET request
        try {
            ResponseEntity<Ticket[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Ticket[].class);
            Ticket[] tickets = response.getBody();

            // Update status tracking panel with ticket data
            if (tickets != null) {
                // Clear existing rows
                model.setRowCount(0);
                for (Ticket ticket : tickets) {
                    model.addRow(new Object[]{ticket.getId(), ticket.getTitle(), ticket.getCategory(), ticket.getPriority(), ticket.getStatus(), ticket.getCreationDate()});
                }
            } else {
                JOptionPane.showMessageDialog(this, "No tickets found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load tickets. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // Method to Load Application Icon
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

        //Parse the creation date correctly
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //LocalDateTime creationDate = LocalDateTime.parse(creationDateStr, formatter);

        // Create the Ticket object with the form values
        Ticket ticket = new Ticket(null, title, description, priority, category, creationDate, "New");

        // Create RestTemplate with authentication
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tickets";

        // Set headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");  // Replace with correct credentials
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request entity
        HttpEntity<Ticket> request = new HttpEntity<>(ticket, headers);

        // Send the POST request
        try {
            ResponseEntity<Ticket> response = restTemplate.exchange(url, HttpMethod.POST, request, Ticket.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Ticket createdTicket = response.getBody();
                JOptionPane.showMessageDialog(this, "Ticket Created:\n" +
                        "Title: " + createdTicket.getTitle() + "\n" +
                        "Description: " + createdTicket.getDescription() + "\n" +
                        "Priority: " + createdTicket.getPriority() + "\n" +
                        "Category: " + createdTicket.getCategory() + "\n" +
                        "Date: " + createdTicket.getCreationDate(), "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reload tickets to update the status tracking panel
                loadTickets((DefaultTableModel) ((JTable) statusTable).getModel());
            }
            else {
                JOptionPane.showMessageDialog(this, "Error creating ticket. Server returned: " + response.getStatusCode(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Reset form fields after submission
        titleField.setText("");
        descriptionArea.setText("");
        priorityComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new mainTicket().setVisible(true));
    }
}