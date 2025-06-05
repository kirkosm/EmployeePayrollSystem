package com.university.ergasiae;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Κλάση για το παράθυρο σύνδεσης χρήστη.
 */
public class Login extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public Login() {
        super("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 180);
        setLocationRelativeTo(null); // Κέντρο οθόνης
        initUI();
    }

    // Δημιουργία και εμφάνιση των στοιχείων του login form
    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Username
        panel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        // Password
        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        // Κενό για στοίχιση και κουμπί Login
        panel.add(new JLabel()); // απλά για να κρατήσει θέση
        btnLogin = new JButton("Login");
        panel.add(btnLogin);

        // Ανάθεση ενέργειας στο κουμπί
        btnLogin.addActionListener(e -> loginUser());

        setContentPane(panel);
    }

    // Ενέργεια όταν ο χρήστης πατήσει Login
    private void loginUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Αν τα στοιχεία είναι σωστά, άνοιξε το MainMenu
                    SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
                    dispose(); // Κλείσε το παράθυρο login
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Λάθος username ή password",
                            "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Σφάλμα βάσης: " + ex.getMessage(),
                    "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }
}
