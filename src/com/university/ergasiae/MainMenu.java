package com.university.ergasiae;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//imports για Excel
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//imports για Word (Docx)
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class MainMenu extends JFrame {
    private JTabbedPane tabs;
    private JPanel controlPanel;
    private JButton btnInsert, btnSearch, btnUpdate;
    private JTextField txtSearch;

    private final Map<String,String> tableMap = new LinkedHashMap<>();

    public MainMenu() {
        super("Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        initTableMap();
        initComponents();
        addPayrollTab();

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void initTableMap() {
        tableMap.put("Employees",    "employee");
        tableMap.put("Phones",       "employeephones");
        tableMap.put("Emails",       "employeeemail");
        tableMap.put("Status",       "employeestatus");
        tableMap.put("Leave",        "leavedays");
        tableMap.put("Sick",         "sickdays");
        tableMap.put("Absences",     "unpaidabsences");
        tableMap.put("Work Hours",   "workhours");
        tableMap.put("Children",     "employeechildren");
        tableMap.put("Dept/Pos",     "employeedepartment");
        tableMap.put("Salary",       "salary");
        tableMap.put("Salary Hist",  "salaryhistory");
    }

    private void addPayrollTab() {
        String title = "Payroll";
        JPanel panel = new JPanel(new BorderLayout(10,10));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JComboBox<Integer> cbYear = new JComboBox<>();
        JComboBox<Integer> cbMonth = new JComboBox<>();
        JButton btnGenerate = new JButton("Generate");
        JButton btnExportExcel = new JButton("Export All to Excel");
        JButton btnPrintPayslip = new JButton("Print Payslip");

        for (int y = 2024; y <= 2026; y++) cbYear.addItem(y);
        for (int m = 1; m <= 12;      m++) cbMonth.addItem(m);
        cbYear.setSelectedItem(LocalDate.now().getYear());
        cbMonth.setSelectedItem(LocalDate.now().getMonthValue());

        north.add(new JLabel("Year:"));   north.add(cbYear);
        north.add(new JLabel("Month:"));  north.add(cbMonth);
        north.add(btnGenerate);
        north.add(btnExportExcel);
        north.add(btnPrintPayslip);

        DefaultTableModel payrollModel = new DefaultTableModel();
        JTable tblPayroll = new JTable(payrollModel);
        tblPayroll.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(tblPayroll);

        panel.add(north,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        tabs.addTab(title, panel);

        Runnable loadData = () -> {
            int year  = (Integer)cbYear.getSelectedItem();
            int month = (Integer)cbMonth.getSelectedItem();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(
                     "SELECT employee_id, salary_type, amount, `year`, `month`, leavedays, sickdays, absencedays " +
                     "FROM salary WHERE `year` = ? AND `month` = ?")) {
                pst.setInt(1, year);
                pst.setInt(2, month);
                try (ResultSet rs = pst.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    payrollModel.setRowCount(0);
                    payrollModel.setColumnCount(0);
                    for (int i = 1; i <= cols; i++) {
                        payrollModel.addColumn(md.getColumnName(i));
                    }
                    while (rs.next()) {
                        Object[] row = new Object[cols];
                        for (int i = 1; i <= cols; i++) {
                            row[i-1] = rs.getObject(i);
                        }
                        payrollModel.addRow(row);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error loading payroll: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        loadData.run();

        btnGenerate.addActionListener(e -> {
            int year  = (Integer)cbYear.getSelectedItem();
            int month = (Integer)cbMonth.getSelectedItem();
            try {
                new com.university.ergasiae.service.PayrollGenerator()
                    .generatePayroll(year, month);
                loadData.run();
                JOptionPane.showMessageDialog(this,
                    "Payroll generated for " + month + "/" + year,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (HeadlessException | SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error generating payroll: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExportExcel.addActionListener(e -> {
    int year  = (Integer) cbYear.getSelectedItem();
    int month = (Integer) cbMonth.getSelectedItem();

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(
             "SELECT s.employee_id, e.first_name, e.last_name, e.address, e.afm, e.amka, " +
             "s.salary_type, s.amount, s.`year`, s.`month`, " +
             "s.leavedays, s.sickdays, s.absencedays " +
             "FROM salary s JOIN employee e ON s.employee_id = e.employee_id " +
             "WHERE s.`year` = ? AND s.`month` = ?"
         )) {

        pst.setInt(1, year);
        pst.setInt(2, month);

        try (ResultSet rs = pst.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this,
                    "Δεν υπάρχουν δεδομένα για εξαγωγή.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Excel File");
            chooser.setSelectedFile(new File("GeneralPayroll_" + month + "_" + year + ".xlsx"));
            int userSelection = chooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) return;
            File fileToSave = chooser.getSelectedFile();

            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                XSSFSheet sheet = wb.createSheet("GeneralPayroll");
                XSSFRow headerRow = sheet.createRow(0);
                for (int col = 1; col <= cols; col++) {
                    headerRow.createCell(col - 1).setCellValue(md.getColumnName(col));
                }

                int rowNum = 1;
                while (rs.next()) {
                    XSSFRow row = sheet.createRow(rowNum++);
                    for (int col = 1; col <= cols; col++) {
                        Object val = rs.getObject(col);
                        if (val instanceof Number number) {
                            row.createCell(col - 1).setCellValue(number.doubleValue());
                        } else if (val != null) {
                            row.createCell(col - 1).setCellValue(val.toString());
                        }
                    }
                }

                for (int i = 0; i < cols; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    wb.write(fos);
                }

                JOptionPane.showMessageDialog(this,
                    "Exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Error exporting to Excel: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
});


        btnPrintPayslip.addActionListener(e -> {
            int selectedRow = tblPayroll.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                    "Επιλέξτε πρώτα έναν υπάλληλο από τον πίνακα Payroll.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DefaultTableModel model = (DefaultTableModel) tblPayroll.getModel();
            int modelRow = tblPayroll.convertRowIndexToModel(selectedRow);

            int empId        = (Integer) model.getValueAt(modelRow, 0);
            String salaryType = model.getValueAt(modelRow, 1).toString();
            BigDecimal netAmount = new BigDecimal(model.getValueAt(modelRow, 2).toString());
            int year         = (Integer) model.getValueAt(modelRow, 3);
            int month        = (Integer) model.getValueAt(modelRow, 4);
            int leaveDays    = (Integer) model.getValueAt(modelRow, 5);
            int sickDays     = (Integer) model.getValueAt(modelRow, 6);
            int absDays      = (Integer) model.getValueAt(modelRow, 7);

            showPayslipDialog(empId, salaryType, netAmount, year, month, leaveDays, sickDays, absDays);
        });
    }
    
    // Μέθοδος που εμφανίζει το Payslip Dialog
    private void showPayslipDialog(int empId,
                                   String salaryType,
                                   BigDecimal netAmount,
                                   int year, int month,
                                   int leaveDays, int sickDays, int absDays) {
        // 1) Πάρε προσωπικά στοιχεία του υπαλλήλου από τον πίνακα employee
        String firstName = "", lastName = "", afm = "", address = "", amka = "";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT first_name, last_name, afm, address, amka " +
                 "FROM employee WHERE employee_id = ?")) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    firstName = rs.getString("first_name");
                    lastName  = rs.getString("last_name");
                    afm       = rs.getString("afm");
                    address   = rs.getString("address");
                    amka      = rs.getString("amka");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error fetching employee details: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String fn = firstName;
        final String ln = lastName;
        final String aAfm = afm;
        final String addr = address;
        final String aAmka = amka;
        final String sType = salaryType;
        final BigDecimal netAmt = netAmount;
        final int yr = year;
        final int mo = month;
        final int lDays = leaveDays;
        final int sDays = sickDays;
        final int abDays = absDays;

        // 2) Δημιουργία modal dialog
        JDialog dialog = new JDialog(this, "Απόδειξη Μισθοδοσίας", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10,10));

        // Τίτλος στο πάνω μέρος
        JLabel lblTitle = new JLabel("ΑΠΟΔΕΙΞΗ ΜΙΣΘΟΔΟΣΙΑΣ", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        dialog.add(lblTitle, BorderLayout.NORTH);

        // Κεντρικό panel με GridLayout
        JPanel center = new JPanel(new GridLayout(10, 2, 8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        center.add(new JLabel("Κωδικός Υπαλλήλου:"));
        center.add(new JLabel(String.valueOf(empId)));

        center.add(new JLabel("Ονοματεπώνυμο:"));
        center.add(new JLabel(fn + " " + ln));

        center.add(new JLabel("ΑΦΜ:"));
        center.add(new JLabel(aAfm));

        center.add(new JLabel("Διεύθυνση:"));
        center.add(new JLabel(addr));

        center.add(new JLabel("ΑΜΚΑ:"));
        center.add(new JLabel(aAmka));

        center.add(new JLabel("Τύπος Μισθού:"));
        center.add(new JLabel(sType));

        center.add(new JLabel("Καθαρός Μισθός:"));
        center.add(new JLabel(netAmt + " €"));

        center.add(new JLabel("Ημέρες Άδειας (Ασθενείας):"));
        center.add(new JLabel(String.valueOf(sDays)));

        center.add(new JLabel("Ημέρες Άδειας (Κανονική):"));
        center.add(new JLabel(String.valueOf(lDays)));

        center.add(new JLabel("Ημέρες Άδειας (Ανεύ Αποδοχών):"));
        center.add(new JLabel(String.valueOf(abDays)));

        dialog.add(center, BorderLayout.CENTER);

        // 3) Κάτω panel με κουμπιά: Print Receipt (Word) και Close
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        // Κουμπί “Print Receipt” (εξαγωγή σε Word)
        JButton btnPrintReceipt = new JButton("Print Receipt");
        btnPrintReceipt.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Payslip as Word (.docx)");
            chooser.setSelectedFile(
                new File("Payslip_Emp" + empId + "_" + yr + "_" + mo + ".docx")
            );
            int userSelection = chooser.showSaveDialog(dialog);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File fileToSave = chooser.getSelectedFile();

            try (XWPFDocument doc = new XWPFDocument()) {
                // Τίτλος
                XWPFParagraph titlePara = doc.createParagraph();
                titlePara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText("ΑΠΟΔΕΙΞΗ ΜΙΣΘΟΔΟΣΙΑΣ");
                titleRun.setBold(true);
                titleRun.setFontSize(16);
                titleRun.addBreak();

                // Πληροφορίες υπαλλήλου
                XWPFParagraph infoPara = doc.createParagraph();
                XWPFRun run = infoPara.createRun();
                run.setText("Κωδικός Υπαλλήλου: " + empId); run.addBreak();
                run.setText("Ονοματεπώνυμο       : " + fn + " " + ln); run.addBreak();
                run.setText("ΑΦΜ                  : " + aAfm); run.addBreak();
                run.setText("Διεύθυνση            : " + addr); run.addBreak();
                run.setText("ΑΜΚΑ                 : " + aAmka); run.addBreak();
                run.addBreak();
                run.setText("Μισθοδοσία για: " + mo + "/" + yr); run.addBreak();
                run.addBreak();
                run.setText("Τύπος Μισθού         : " + sType); run.addBreak();
                run.setText("Καθαρός Μισθός       : " + netAmt + " €"); run.addBreak();
                run.setText("Ημ. Άδειας (Ασθεν.)  : " + sDays); run.addBreak();
                run.setText("Ημ. Άδειας (Κανον.)  : " + lDays); run.addBreak();
                run.setText("Ημ. Άδειας (Άνευ)    : " + abDays); run.addBreak();

                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    doc.write(fos);
                }
                JOptionPane.showMessageDialog(dialog,
                    "Payslip exported to Word:\n" + fileToSave.getAbsolutePath(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error exporting to Word: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Κουμπί “Close”
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(ev -> dialog.dispose());

        south.add(btnPrintReceipt);
        south.add(btnClose);

        dialog.add(south, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void initComponents() {
        tabs = new JTabbedPane();
        for (Map.Entry<String,String> e : tableMap.entrySet()) {
            addOrReloadTab(e.getKey(), e.getValue(), null, null);
        }

        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnInsert = new JButton("Insert");
        txtSearch = new JTextField(15);
        btnSearch = new JButton("Αναζήτηση");
        btnUpdate = new JButton("Update");

        controlPanel.add(btnInsert);
        controlPanel.add(new JLabel("Search:"));
        controlPanel.add(txtSearch);
        controlPanel.add(btnSearch);
        controlPanel.add(btnUpdate);

        btnInsert.addActionListener(e -> showInsertDialog());
        btnSearch.addActionListener(e -> onSearch());
        btnUpdate.addActionListener(e -> onManualEdit());
    }

    // Ποιο table βλέπει τώρα ο χρήστης;
    private String getCurrentTableName() {
        String title = tabs.getTitleAt(tabs.getSelectedIndex());
        return tableMap.get(title);
    }

    // Η JTable μέσα στο τρέχον tab
    private JTable getCurrentTableComponent() {
        JScrollPane scroll = (JScrollPane)tabs.getComponentAt(tabs.getSelectedIndex());
        return (JTable)scroll.getViewport().getView();
    }

    // Προσθέτει ή ξαναφορτώνει tab
    private void addOrReloadTab(String title, String tableName, String whereClause, String[] params) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM " + tableName
                       + (whereClause != null ? " WHERE " + whereClause : "");
            PreparedStatement pst = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pst.setString(i+1, params[i]);
                }
            }
            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = buildEditableModel(rs, tableName);
                JTable table = new JTable(model);
                table.setAutoCreateRowSorter(true);
                attachInlineUpdateListener(table, tableName);

                JScrollPane scroll = new JScrollPane(table);
                int idx = tabs.indexOfTab(title);
                if (idx >= 0) {
                    tabs.setComponentAt(idx, scroll);
                } else {
                    tabs.addTab(title, scroll);
                }
            }
        } catch (SQLException ex) {
            JLabel lbl = new JLabel("Error loading " + tableName + ": " + ex.getMessage());
            int idx = tabs.indexOfTab(title);
            if (idx >= 0) tabs.setComponentAt(idx, lbl);
            else tabs.addTab(title, lbl);
        }
    }

    // Δημιουργεί Editable TableModel
    private DefaultTableModel buildEditableModel(ResultSet rs, String tableName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int colCount = md.getColumnCount();
        List<String> pkCols = getPrimaryKeys(tableName);

        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) {
                String colName = null;
                try {
                    colName = md.getColumnName(col+1);
                } catch (SQLException ex) {
                    Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                }
                return !pkCols.contains(colName);
            }
        };
        for (int i = 1; i <= colCount; i++) {
            model.addColumn(md.getColumnName(i));
        }
        while (rs.next()) {
            Object[] row = new Object[colCount];
            for (int i = 1; i <= colCount; i++) {
                row[i-1] = rs.getObject(i);
            }
            model.addRow(row);
        }
        return model;
    }

    // Παίρνει primary key columns
    private List<String> getPrimaryKeys(String table) throws SQLException {
        List<String> pks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData dmd = conn.getMetaData();
            try (ResultSet pkRs = dmd.getPrimaryKeys(null, null, table)) {
                while (pkRs.next()) {
                    pks.add(pkRs.getString("COLUMN_NAME"));
                }
            }
        }
        return pks;
    }

    // INLINE update listener: χρησιμοποιούμε JTable.getColumnModel().getColumnIndex(...)
    private void attachInlineUpdateListener(final JTable table, String tableName) {
        table.getModel().addTableModelListener(e -> {
            if (e.getType() != TableModelEvent.UPDATE) return;
            int row = e.getFirstRow();
            int col = e.getColumn();
            TableModel m = table.getModel();
            String colName = m.getColumnName(col);
            Object newVal = m.getValueAt(row, col);

            try {
                List<String> pks = getPrimaryKeys(tableName);
                String pk = pks.get(0); 

                int pkColIndex = table.getColumnModel().getColumnIndex(pk);
                Object pkVal = m.getValueAt(row, pkColIndex);

                String sql = "UPDATE " + tableName +
                             " SET " + colName + " = ? WHERE " + pk + " = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setObject(1, newVal);
                    pst.setObject(2, pkVal);
                    pst.executeUpdate();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Update failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Fully-dynamic INSERT dialog
    private void showInsertDialog() {
        String table = getCurrentTableName();
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet cols = dmd.getColumns(null, null, table, null);

            List<String> colNames = new ArrayList<>();
            List<Boolean> isAuto  = new ArrayList<>();
            while (cols.next()) {
                colNames.add(cols.getString("COLUMN_NAME"));
                isAuto.add("YES".equals(cols.getString("IS_AUTOINCREMENT")));
            }

            JPanel form = new JPanel(new GridLayout(0,2,5,5));
            List<JTextField> fields = new ArrayList<>();
            for (int i = 0; i < colNames.size(); i++) {
                if (isAuto.get(i)) continue;
                form.add(new JLabel(colNames.get(i)));
                JTextField tf = new JTextField();
                form.add(tf);
                fields.add(tf);
            }

            int opt = JOptionPane.showConfirmDialog(this, form,
                "Insert into " + table, JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                List<String> insCols = new ArrayList<>();
                for (int i = 0; i < colNames.size(); i++)
                    if (!isAuto.get(i)) insCols.add(colNames.get(i));

                String colList = String.join(",", insCols);
                String qMarks  = insCols.stream().map(c->"?").collect(Collectors.joining(","));
                String sql = "INSERT INTO " + table + "(" + colList + ") VALUES(" + qMarks + ")";

                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    for (int i = 0; i < fields.size(); i++) {
                        pst.setString(i+1, fields.get(i).getText());
                    }
                    pst.executeUpdate();
                    addOrReloadTab(tabs.getTitleAt(tabs.getSelectedIndex()), table, null, null);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Insert failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Fully-dynamic SEARCH (Αναζήτηση)
    private void onSearch() {
        String term = txtSearch.getText().trim();
        String table = getCurrentTableName();
        if (term.isEmpty()) {
            addOrReloadTab(tabs.getTitleAt(tabs.getSelectedIndex()), table, null, null);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet cols = dmd.getColumns(null, null, table, null);
            List<String> searchCols = new ArrayList<>();
            while (cols.next()) {
                String c    = cols.getString("COLUMN_NAME");
                String type = cols.getString("TYPE_NAME");
                if (type.contains("CHAR") || type.contains("TEXT")
                 || type.contains("INT")  || type.contains("DATE")) {
                    searchCols.add(c);
                }
            }
            if (searchCols.isEmpty()) return;

            String where = searchCols.stream()
                                     .map(c->c+" LIKE ?")
                                     .collect(Collectors.joining(" OR "));
            String[] params = new String[searchCols.size()];
            Arrays.fill(params, "%"+term+"%");

            addOrReloadTab(tabs.getTitleAt(tabs.getSelectedIndex()), table, where, params);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Search failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Manual Edit popup dialog
    private void onManualEdit() {
        JTable table = getCurrentTableComponent();
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Επίλεξε πρώτα μια γραμμή για επεξεργασία.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String tableName = getCurrentTableName();

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet cols = dmd.getColumns(null, null, tableName, null);
            List<String> colNames = new ArrayList<>();
            List<Boolean> isPk    = new ArrayList<>();
            while (cols.next()) {
                String col = cols.getString("COLUMN_NAME");
                colNames.add(col);
                isPk.add(getPrimaryKeys(tableName).contains(col));
            }

            JPanel form = new JPanel(new GridLayout(0,2,5,5));
            List<JTextField> fields = new ArrayList<>();
            for (int i = 0; i < colNames.size(); i++) {
                form.add(new JLabel(colNames.get(i)));
                Object val = table.getValueAt(row,
                    table.getColumnModel().getColumnIndex(colNames.get(i)));
                JTextField tf = new JTextField(val == null ? "" : val.toString());
                tf.setEditable(!isPk.get(i));
                form.add(tf);
                fields.add(tf);
            }

            int opt = JOptionPane.showConfirmDialog(this, form,
                "Edit row in " + tableName, JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                List<String> setters = new ArrayList<>();
                String pkCol = null; Object pkVal = null;
                for (int i = 0; i < colNames.size(); i++) {
                    if (isPk.get(i)) {
                        pkCol = colNames.get(i);
                        pkVal = fields.get(i).getText();
                    } else {
                        setters.add(colNames.get(i) + " = ?");
                    }
                }
                String sql = "UPDATE " + tableName + " SET "
                           + String.join(",", setters)
                           + " WHERE " + pkCol + " = ?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    int idx = 1;
                    for (int i = 0; i < colNames.size(); i++) {
                        if (!isPk.get(i)) {
                            pst.setString(idx++, fields.get(i).getText());
                        }
                    }
                    pst.setString(idx, pkVal.toString());
                    pst.executeUpdate();
                }
                addOrReloadTab(tabs.getTitleAt(tabs.getSelectedIndex()), tableName, null, null);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Edit failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
