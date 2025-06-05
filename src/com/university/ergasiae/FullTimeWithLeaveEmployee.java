package com.university.ergasiae;

import java.time.LocalDate;
import java.util.*;

// Full-time υπάλληλος που μπορεί να έχει:
// Κανονική άδεια (regular leave)
// Άδεια άνευ αποδοχών (unpaid leave)
public class FullTimeWithLeaveEmployee extends FullTimeEmployee {

    // Ημερομηνίες με κανονική άδεια
    private final Set<LocalDate> regularLeave;

    // Ημερομηνίες με άδεια άνευ αποδοχών
    private final Set<LocalDate> unpaidLeave;

    // Κατασκευαστής για full-time υπάλληλο με άδειες
    public FullTimeWithLeaveEmployee(int id, String fn, String ln,
                                     List<LocalDate> regularLeave,
                                     List<LocalDate> unpaidLeave) {
        super(id, fn, ln);
        this.regularLeave = new HashSet<>(regularLeave);
        this.unpaidLeave  = new HashSet<>(unpaidLeave);
    }

    // Υπολογίζει πόσες ώρες εργάζεται την ημέρα.
    // Αν είναι σε κανονική ή άδεια άνευ αποδοχών, δεν εργάζεται καθόλου.
    @Override
    protected double hoursOn(LocalDate date) {
        double baseHours = super.hoursOn(date); // συνήθως 8 ώρες
        if (baseHours == 0.0) return 0.0;

        // Αν η μέρα είναι άδεια, δεν δουλεύει
        if (regularLeave.contains(date) || unpaidLeave.contains(date)) {
            return 0.0;
        }

        return baseHours;
    }
}
