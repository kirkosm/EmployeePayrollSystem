package com.university.ergasiae;

import java.time.*;

// Υποκλάση για ωρομίσθιους υπαλλήλους.
// Δουλεύουν 3 ώρες τις Δευτέρες, Τρίτες και Πέμπτες.
public class HourlyEmployee extends Employee {

    // Ποσό αμοιβής ανά ώρα
    private final double hourlyRate;

    public HourlyEmployee(int id, String fn, String ln, double hourlyRate) {
        super(id, fn, ln);
        this.hourlyRate = hourlyRate;
    }

    // Πόσες ώρες εργάζεται την ημέρα αυτή;
    // Δευτέρα, Τρίτη και Πέμπτη → 3 ώρες
    @Override
    protected double hoursOn(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.MONDAY ||
            day == DayOfWeek.TUESDAY ||
            day == DayOfWeek.THURSDAY) {
            return 3.0;
        }
        return 0.0;
    }

    // Επιστρέφει την ωριαία αμοιβή.
    public double getHourlyRate() {
        return hourlyRate;
    }
}
