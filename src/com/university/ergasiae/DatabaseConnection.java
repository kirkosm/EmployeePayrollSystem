package com.university.ergasiae;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Κλάση για την σύνδεση με τη βάση δεδομένων MySQL
public class DatabaseConnection {

    // Διεύθυνση σύνδεσης στη βάση (όνομα DB: ErgasiaE)
    private static final String URL = "jdbc:mysql://localhost:3306/ErgasiaE";

    // Username & password της βάσης (προσαρμόστε αν χρειάζεται)
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    // Φόρτωση του MySQL driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Αν αποτύχει, εμφανίζουμε το σφάλμα
            
        }
    }

    //Επιστρέφει αντικείμενο σύνδεσης με τη βάση δεδομένων
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
