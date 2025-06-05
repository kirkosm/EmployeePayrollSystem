package com.university.ergasiae.service;

import com.university.ergasiae.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

// Υπολογίζει τη μισθοδοσία κάθε υπαλλήλου για συγκεκριμένο μήνα και έτος
// Χρησιμοποιεί δεδομένα από salaryhistory, leavedays, sickdays, unpaidabsences
public class PayrollGenerator {

    public void generatePayroll(int year, int month) throws SQLException {
        // Υπολογισμός αρχής και τέλους του μήνα
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        // Ερωτήματα SQL
        String deleteSql =
            "DELETE FROM salary WHERE `year` = ? AND `month` = ?";

        String selectEmpSql =
            "SELECT employee_id FROM employee";

        String selectHistSql =
            "SELECT monthly_amount FROM salaryhistory " +
            "WHERE employee_id = ? " +
            "ORDER BY pay_date DESC LIMIT 1";

        String sumLeaveSql =
            "SELECT COALESCE(SUM(total_days),0) FROM leavedays " +
            "WHERE employee_id = ? AND start_date BETWEEN ? AND ?";

        String sumSickSql =
            "SELECT COALESCE(SUM(total_days),0) FROM sickdays " +
            "WHERE employee_id = ? AND start_date BETWEEN ? AND ?";

        String sumUnpaidSql =
            "SELECT COALESCE(SUM(total_days),0) FROM unpaidabsences " +
            "WHERE employee_id = ? AND start_date BETWEEN ? AND ?";

        String insertSql =
            "INSERT INTO salary " +
            "(employee_id, salary_type, amount, `year`, `month`, leavedays, sickdays, absencedays) " +
            "VALUES (?,?,?,?,?,?,?,?)";

        // Εκτέλεση SQL
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psDel  = conn.prepareStatement(deleteSql);
             PreparedStatement psEmp  = conn.prepareStatement(selectEmpSql);
             PreparedStatement psHist = conn.prepareStatement(selectHistSql);
             PreparedStatement psSumL = conn.prepareStatement(sumLeaveSql);
             PreparedStatement psSumS = conn.prepareStatement(sumSickSql);
             PreparedStatement psSumU = conn.prepareStatement(sumUnpaidSql);
             PreparedStatement psIns  = conn.prepareStatement(insertSql)) {

            // Διαγραφή προηγούμενης μισθοδοσίας για τον συγκεκριμένο μήνα
            psDel.setInt(1, year);
            psDel.setInt(2, month);
            psDel.executeUpdate();

            try (ResultSet rsE = psEmp.executeQuery()) {
                while (rsE.next()) {
                    int empId = rsE.getInt("employee_id");
                    String salaryType = "monthly";
                    BigDecimal baseMonthly = BigDecimal.ZERO;

                    // Ανάκτηση τελευταίου μισθού από salaryhistory
                    psHist.setInt(1, empId);
                    try (ResultSet rsH = psHist.executeQuery()) {
                        if (!rsH.next()) {
                            System.out.println("  - No salaryhistory (monthly_amount) for emp=" + empId + ". Skipping.");
                            continue;
                        }
                        baseMonthly = rsH.getBigDecimal("monthly_amount");
                    }

                    // Υπολογισμός ημερών άδειας, ασθενείας, άνευ αποδοχών
                    int leaveDays = 0, sickDays = 0, absDays = 0;

                    psSumL.setInt(1, empId);
                    psSumL.setDate(2, Date.valueOf(start));
                    psSumL.setDate(3, Date.valueOf(end));
                    try (ResultSet rsL = psSumL.executeQuery()) {
                        if (rsL.next()) leaveDays = rsL.getInt(1);
                    }

                    psSumS.setInt(1, empId);
                    psSumS.setDate(2, Date.valueOf(start));
                    psSumS.setDate(3, Date.valueOf(end));
                    try (ResultSet rsS = psSumS.executeQuery()) {
                        if (rsS.next()) sickDays = rsS.getInt(1);
                    }

                    psSumU.setInt(1, empId);
                    psSumU.setDate(2, Date.valueOf(start));
                    psSumU.setDate(3, Date.valueOf(end));
                    try (ResultSet rsU = psSumU.executeQuery()) {
                        if (rsU.next()) absDays = rsU.getInt(1);
                    }

                    // Υπολογισμός καθαρού μισθού με βάση τις απουσίες άνευ αποδοχών
                    BigDecimal dailyRate = baseMonthly.divide(BigDecimal.valueOf(25), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal deduction = dailyRate.multiply(BigDecimal.valueOf(absDays));
                    BigDecimal netSalary = baseMonthly.subtract(deduction);

                    // Εισαγωγή μισθοδοσίας στον πίνακα salary
                    psIns.setInt(1, empId);
                    psIns.setString(2, salaryType);
                    psIns.setBigDecimal(3, netSalary);
                    psIns.setInt(4, year);
                    psIns.setInt(5, month);
                    psIns.setInt(6, leaveDays);
                    psIns.setInt(7, sickDays);
                    psIns.setInt(8, absDays);
                    psIns.executeUpdate();
                }
            }
        }
    }
}
