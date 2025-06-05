package com.university.ergasiae;

import javax.swing.SwingUtilities;

/**
 * Κύρια κλάση εκκίνησης της εφαρμογής.
 */
public class Main {
    public static void main(String[] args) {
        // Εκτελούμε το GUI στο ειδικό thread για γραφικό περιβάλλον (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            // Εμφάνιση παραθύρου σύνδεσης
            Login loginWindow = new Login();
            loginWindow.setVisible(true);
        });
    }
}
