package com.university.ergasiae;

import java.time.DayOfWeek;
import java.time.LocalDate;

// Υπάλληλος πλήρους απασχόλησης:
// Εργάζεται 8 ώρες κάθε Δευτέρα έως Παρασκευή.
public class FullTimeEmployee extends Employee {

    // Κατασκευαστής για πλήρους απασχόλησης υπάλληλο.
    public FullTimeEmployee(int id, String fn, String ln) {
        super(id, fn, ln);
    }

    // Επιστρέφει τις ώρες εργασίας για μια συγκεκριμένη ημερομηνία.
    // Αν η μέρα είναι εργάσιμη (Δευ–Παρ), επιστρέφει 8.0 ώρες.
    // Αν είναι Σάββατο ή Κυριακή, επιστρέφει 0.0 ώρες.
    @Override
    protected double hoursOn(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return 0.0;
        }
        return 8.0;
    }
}
