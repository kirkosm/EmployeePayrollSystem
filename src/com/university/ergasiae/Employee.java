package com.university.ergasiae;

import java.time.LocalDate;
import java.util.*;

// Κάθε υπάλληλος έχει κωδικό, όνομα και επώνυμο
public abstract class Employee {

    protected int id;
    protected String firstName;
    protected String lastName;

    // Κατασκευαστής υπαλλήλου
    public Employee(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Επιστρέφει τον κωδικό υπαλλήλου
    public int getId() {
        return id;
    }

    // Επιστρέφει το πλήρες όνομα του υπαλλήλου
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Δημιουργεί έναν χάρτη με τις ημερομηνίες και τις ώρες εργασίας του υπαλλήλου,
    // για το διάστημα από την start μέχρι την end.
    // Αν μια μέρα δεν είναι εργάσιμη (ώρες = 0), τότε δεν περιλαμβάνεται στον χάρτη.
    public Map<LocalDate, Double> generateWorkHours(LocalDate start, LocalDate end) {
        Map<LocalDate, Double> hoursMap = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            double hours = hoursOn(date);
            if (hours > 0) {
                hoursMap.put(date, hours);
            }
        }
        return hoursMap;
    }

    // Eπιστρέφει τις ώρες εργασίας του υπαλλήλου
    protected abstract double hoursOn(LocalDate date);
}
