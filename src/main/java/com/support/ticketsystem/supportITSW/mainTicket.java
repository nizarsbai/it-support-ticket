package com.support.ticketsystem.supportITSW;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.support.ticketsystem.entity.User;
import com.support.ticketsystem.model.AuditLog;
import com.support.ticketsystem.model.Ticket;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class mainTicket extends JFrame {
    private List<AuditLog> auditLog = new ArrayList<>();  // List to store the audit logs
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
    private User currentUser;

    public mainTicket(User user) {
        this.currentUser = user;
        auditLog = new ArrayList<>(); // Initialize the in-memory log
        initComponents();
    }
    public mainTicket() {
        initComponents();
    }

    public void initComponents() {
        setTitle("Support IT Ticket System - " + (currentUser != null ? currentUser.getUsername() : "Guest"));
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

        // Create the Audit Log Panel
        JPanel auditLogPanel = createAuditLogPanel();
        tabbedPane.addTab("Audit Log", auditLogPanel);


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

    private void filterTickets(String ticketId, String status) {
        DefaultTableModel model = (DefaultTableModel) statusTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        statusTable.setRowSorter(sorter);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Filter by Ticket ID (if not empty)
        if (!ticketId.isEmpty()) {
            filters.add(RowFilter.regexFilter("^" + ticketId + "$", 0)); // Exact match on Ticket ID column (index 0)
        }

        // Filter by Status (if not "All")
        if (!status.equals("All")) {
            filters.add(RowFilter.regexFilter(status, 4)); // Status column is index 4
        }

        // Apply filters
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }


    // Method to create the Status Tracking Panel
    private JPanel createStatusTrackingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create a panel for search inputs
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Search by Ticket ID
        JLabel idLabel = new JLabel("Ticket ID:");
        JTextField idField = new JTextField(10); // Input field for Ticket ID

        // Search by Status
        JLabel statusLabel = new JLabel("Status:");
        JComboBox<String> statusComboBox = new JComboBox<>(new String[] {"All", "New", "In Progress", "Resolved"});

        // Search Button
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {
            String searchId = idField.getText().trim();
            String selectedStatus = (String) statusComboBox.getSelectedItem();
            filterTickets(searchId, selectedStatus);

        });

        // Add components to search panel
        searchPanel.add(idLabel);
        searchPanel.add(idField);
        searchPanel.add(statusLabel);
        searchPanel.add(statusComboBox);
        searchPanel.add(searchButton);

        // Table for displaying tickets
        String[] columnNames = {"Ticket ID", "Title", "Category", "Priority", "Status", "Creation Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        statusTable = new JTable(model); // Assign to class-level variable

        // Fetch tickets from the server and display in the table
        loadTickets(model);

        // Make the "Status" column editable with a combo box
        JComboBox<String> statusEditorComboBox = new JComboBox<>(new String[] {"New", "In Progress", "Resolved"});
        statusTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusEditorComboBox));

        // Add an editor listener to update the status when it changes
        statusTable.getColumnModel().getColumn(4).getCellEditor().addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                int row = statusTable.getSelectedRow();
                if (row == -1) return;

                String newStatus = (String) statusTable.getValueAt(row, 4);
                Long ticketId = (Long) statusTable.getValueAt(row, 0);

                if (ticketId != null && newStatus != null) {
                    Ticket ticketDTO = new Ticket(ticketId,
                            (String) statusTable.getValueAt(row, 1),
                            (String) statusTable.getValueAt(row, 2),
                            (String) statusTable.getValueAt(row, 3),
                            (String) statusTable.getValueAt(row, 4),
                            (String) statusTable.getValueAt(row, 5),
                            newStatus
                    );
                    updateTicketStatus(ticketDTO);
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) {}
        });

        JScrollPane scrollPane = new JScrollPane(statusTable);

        // Add search panel on top of the table
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuditLogPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create table for displaying audit logs
        String[] columnNames = {"Ticket ID", "Old Status", "New Status", "Comment", "Timestamp"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable auditLogTable = new JTable(model);

        // Load logs into the table
        loadAuditLogs(model);

        JScrollPane scrollPane = new JScrollPane(auditLogTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


    private void loadAuditLogs(DefaultTableModel model) {
        // Clear existing rows in the table
        model.setRowCount(0);

        // Ajout de valeurs statiques pour l'affichage
        model.addRow(new Object[]{"1", "New", "In Progress", "Ticket assigned to IT support", "2025-02-24 10:00:00"});
        model.addRow(new Object[]{"2", "In Progress", "Resolved", "Issue fixed by support", "2025-02-24 11:15:30"});
        model.addRow(new Object[]{"3", "New", "Resolved", "Immediate resolution applied", "2025-02-24 12:45:10"});

        // Print the auditLog list for debugging
        System.out.println("Audit Log Entries: " + auditLog);

        // Add each audit log entry from the list to the table
        for (AuditLog log : auditLog) {
            model.addRow(new Object[]{
                    log.getTicketId(),
                    log.getOldStatus(),
                    log.getNewStatus(),
                    log.getComment(),
                    log.getTimestamp()
            });
        }
    }



    private void updateStatus(Ticket ticketDTO) {
        // Get the old status from the table
        String oldStatus = (String) statusTable.getValueAt(statusTable.getSelectedRow(), 4);

        // Update the ticket status (this would update the ticket object itself)
        //ticket.setStatus(newStatus); // Assuming you have a newStatus variable that you want to set
    }
    public void logAudit(Ticket ticketDTO, String oldStatus, String comment) {
        System.out.println("Logging audit for ticket " + ticketDTO.getId() + " with status change from " + oldStatus + " to " + ticketDTO.getStatus());
        auditLog.add(new AuditLog(ticketDTO.getId(), oldStatus, ticketDTO.getStatus(), comment, getCurrentDate()));

        auditLog.add(new AuditLog(ticketDTO.getId(), oldStatus, ticketDTO.getStatus(), comment, getCurrentDate()));
        System.out.println("Audit Log size: " + auditLog.size());

    }






    private void handleTicketStatusUpdate(Ticket ticketDTO) {
        // Get the old status before updating
        String oldStatus = ticketDTO.getStatus();

        // Update the ticket status (this changes the status of the ticket)
        updateStatus(ticketDTO);

        // Log the audit entry with old status and new status
        String comment = "Status updated"; // You can adjust this based on the specific update
        logAudit(ticketDTO, oldStatus, comment);

        // Refresh the table to display updated data
        loadAuditLogs((DefaultTableModel) ((JTable) ((JScrollPane) tabbedPane.getComponentAt(2)).getViewport().getView()).getModel());
    }

    public void updateStatusForTicket(Ticket ticketDTO) {
        // Now you can use the ticket object
        String oldStatus = ticketDTO.getStatus();  // Capture old status
        ticketDTO.setStatus("New");         // Update ticket status

        // Log the audit entry
        logAudit(ticketDTO, oldStatus, "Status updated to: New");

        // Refresh the audit log table
        handleTicketStatusUpdate(ticketDTO);
    }





    private void updateTicketStatus(Ticket ticketDTO) {
        // Ensure ticket ID is not null
        if (ticketDTO.getId() == null) {
            JOptionPane.showMessageDialog(this, "Ticket ID is missing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //oldStatus = (String) statusTable.getValueAt(statusTable.getSelectedRow(), 4); // Get the old status from the table

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tickets/" + ticketDTO.getId(); // Assuming the URL includes ticket ID (as Long)

        // Add headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin"); // Replace with your Spring Security credentials
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Ticket> request = new HttpEntity<>(ticketDTO, headers);

        try {
            // Print for debugging
            System.out.println("Updating ticket with ID: " + ticketDTO.getId());
            System.out.println("URL: " + url);

            // Send PUT request to update the ticket
            ResponseEntity<Ticket> response = restTemplate.exchange(url, HttpMethod.PUT, request, Ticket.class);

            // Check if the request was successful
            if (response.getStatusCode().is2xxSuccessful()) {
                Ticket updatedTicketDTO = response.getBody();
                JOptionPane.showMessageDialog(this, "Ticket status updated successfully:\n" +
                        "ID: " + updatedTicketDTO.getId() + "\n" +
                        "New Status: " + updatedTicketDTO.getStatus(), "Success", JOptionPane.INFORMATION_MESSAGE);

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
            Ticket[] ticketDTOS = response.getBody();

            // Update status tracking panel with ticket data
            if (ticketDTOS != null) {
                // Clear existing rows
                model.setRowCount(0);
                for (Ticket ticketDTO : ticketDTOS) {
                    model.addRow(new Object[]{ticketDTO.getId(), ticketDTO.getTitle(), ticketDTO.getCategory(), ticketDTO.getPriority(), ticketDTO.getStatus(), ticketDTO.getCreationDate()});
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
        Ticket ticketDTO = new Ticket(null, title, description, priority, category, creationDate, "New");

        // Create RestTemplate with authentication
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tickets";

        // Set headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");  // Replace with correct credentials
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request entity
        HttpEntity<Ticket> request = new HttpEntity<>(ticketDTO, headers);

        // Send the POST request
        try {
            ResponseEntity<Ticket> response = restTemplate.exchange(url, HttpMethod.POST, request, Ticket.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Ticket createdTicketDTO = response.getBody();
                JOptionPane.showMessageDialog(this, "Ticket Created:\n" +
                        "Title: " + createdTicketDTO.getTitle() + "\n" +
                        "Description: " + createdTicketDTO.getDescription() + "\n" +
                        "Priority: " + createdTicketDTO.getPriority() + "\n" +
                        "Category: " + createdTicketDTO.getCategory() + "\n" +
                        "Date: " + createdTicketDTO.getCreationDate(), "Success", JOptionPane.INFORMATION_MESSAGE);

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