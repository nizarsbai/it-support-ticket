package com.support.ticketsystem.supportITSW;

import javax.swing.*;

public class mainTicket extends JFrame {
    public mainTicket() {
    initComponents();
    }
    public void initComponents(){
        setTitle("Support Ticket");
        setSize(600,600);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        new mainTicket().setVisible(true);
    }
}
