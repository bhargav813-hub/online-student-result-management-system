package ui;

import db.DatabaseConnection;
import model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentModule extends JFrame {

    private String rollNo;
    private JLabel lblDetails;
    private DefaultTableModel model;
    private JTable resultTable;

    // Summary stat value labels
    private JLabel statTotal;
    private JLabel statMax;
    private JLabel statPercent;
    private JLabel statGrade;

    /** Called from LoginPage after UserSession is populated. */
    public StudentModule() {
        this(UserSession.getLoggedInUserId());
    }

    /** Called directly with a roll number (e.g. standalone use). */
    public StudentModule(String rollNo) {
        this.rollNo = rollNo;
        setTitle("Student Dashboard - Result Management System");
        setSize(900, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.LIGHT_GRAY);

        buildUI();
        loadResults();
    }

    private void buildUI() {

        // =======================================================================
        // NAVBAR
        // =======================================================================
        add(UIUtils.createNavbar(
                "Student Results",
                UserSession.getLoggedInUserName() + "  |  " + rollNo,
                () -> {
                    UserSession.clear();
                    this.dispose();
                    new LoginPage().setVisible(true);
                }
        ), BorderLayout.NORTH);

        // =======================================================================
        // STUDENT INFO STRIP (light blue banner under navbar)
        // =======================================================================
        lblDetails = new JLabel("Loading student details...", SwingConstants.LEFT);
        lblDetails.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDetails.setForeground(UIUtils.DARK_BLUE);
        lblDetails.setBackground(new Color(219, 234, 254));
        lblDetails.setOpaque(true);
        lblDetails.setBorder(new EmptyBorder(10, 22, 10, 22));

        // =======================================================================
        // MAIN CONTENT
        // =======================================================================
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(UIUtils.LIGHT_GRAY);
        content.setBorder(new EmptyBorder(18, 20, 6, 20));

        // Section header
        content.add(UIUtils.createSectionHeader("Semester Examination Results"), BorderLayout.NORTH);

        // Results table
        model = new DefaultTableModel(
                new String[]{"Subject Name", "Marks Obtained", "Grade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        resultTable = new JTable(model);
        styleResultTable();

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        scroll.getViewport().setBackground(UIUtils.WHITE);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UIUtils.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        tableCard.add(scroll, BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        // Summary stat cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setPreferredSize(new Dimension(0, 96));
        summaryRow.setBorder(new EmptyBorder(4, 0, 0, 0));

        statTotal   = buildStatCard(summaryRow, "Total Marks",   "0",      UIUtils.PRIMARY_BLUE);
        statMax     = buildStatCard(summaryRow, "Maximum Marks", "0",      UIUtils.DARK_BLUE);
        statPercent = buildStatCard(summaryRow, "Percentage",    "0.00%",  UIUtils.SUCCESS_GREEN);
        statGrade   = buildStatCard(summaryRow, "Final Grade",   "-",      UIUtils.WARN_AMBER);

        content.add(summaryRow, BorderLayout.SOUTH);

        // Stack info strip + content
        JPanel centerStack = new JPanel(new BorderLayout());
        centerStack.setBackground(UIUtils.LIGHT_GRAY);
        centerStack.add(lblDetails, BorderLayout.NORTH);
        centerStack.add(content, BorderLayout.CENTER);

        add(centerStack, BorderLayout.CENTER);
    }

    // =======================================================================
    // ORIGINAL LOGIC - unchanged
    // =======================================================================
    private void loadResults() {
        int totalMarks = 0;
        int maxMarks   = 0;
        boolean hasFailed = false;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Fetch Student Info
            PreparedStatement psInfo = conn.prepareStatement(
                    "SELECT name, semester, department FROM students WHERE roll_no=?");
            psInfo.setString(1, rollNo);
            ResultSet rsInfo = psInfo.executeQuery();
            if (rsInfo.next()) {
                lblDetails.setText(
                    "  Name: " + rsInfo.getString("name") +
                    "    |    Roll No: " + rollNo +
                    "    |    Sem: "     + rsInfo.getInt("semester") +
                    "    |    Dept: "    + rsInfo.getString("department"));
            }

            // Fetch Results
            PreparedStatement psRes = conn.prepareStatement(
                    "SELECT s.subject_name, r.marks FROM results r " +
                    "JOIN subjects s ON r.subject_id = s.subject_id WHERE r.roll_no=?");
            psRes.setString(1, rollNo);
            ResultSet rsRes = psRes.executeQuery();

            while (rsRes.next()) {
                int marks = rsRes.getInt("marks");
                String displayMarks;
                String grade;

                // Check if the student failed this specific subject
                if (marks < 35) {
                    hasFailed = true;
                }

                if (marks == -1) {
                    displayMarks = "AB";
                    grade = "F (Absent)";
                    maxMarks += 100;
                } else if (marks == -2) {
                    displayMarks = "MP";
                    grade = "F (Malpractice)";
                    maxMarks += 100;
                } else {
                    displayMarks = String.valueOf(marks);
                    totalMarks  += marks;
                    maxMarks    += 100;
                    grade = calculateGrade(marks);
                }

                model.addRow(new Object[]{
                        rsRes.getString("subject_name"), displayMarks, grade});
            }

            if (maxMarks > 0) {
                double percentage = ((double) totalMarks / maxMarks) * 100;
                // If they failed any subject, overall grade is "Fail" regardless of percentage
                String finalGrade = hasFailed ? "Fail" : calculateGrade((int) percentage);

                statTotal.setText(String.valueOf(totalMarks));
                statMax.setText(String.valueOf(maxMarks));
                statPercent.setText(String.format("%.2f%%", percentage));
                statGrade.setText(finalGrade);

                // Turn grade card red on failure
                if ("Fail".equals(finalGrade) || "F".equals(finalGrade)) {
                    statGrade.setForeground(UIUtils.DANGER_RED);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading results: " + ex.getMessage());
        }
    }

    // Original grade logic - unchanged
    private String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        if (marks >= 35) return "E";
        return "F";
    }

    // =======================================================================
    // UI HELPERS
    // =======================================================================

    private void styleResultTable() {
        resultTable.setFont(UIUtils.NORMAL_FONT);
        resultTable.setRowHeight(34);
        resultTable.setShowGrid(false);
        resultTable.setShowHorizontalLines(true);
        resultTable.setGridColor(UIUtils.BORDER_COLOR);
        resultTable.setBackground(UIUtils.WHITE);
        resultTable.setIntercellSpacing(new Dimension(0, 1));
        resultTable.setSelectionBackground(new Color(219, 234, 254));
        resultTable.setSelectionForeground(UIUtils.TEXT_DARK);

        // Row renderer: highlight failed rows in light red
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setFont(UIUtils.NORMAL_FONT);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    Object gradeVal = table.getModel().getValueAt(row, 2);
                    String grade = gradeVal == null ? "" : gradeVal.toString();
                    if (grade.startsWith("F")) {
                        setBackground(new Color(254, 242, 242));
                        setForeground(new Color(185, 28, 28));
                    } else {
                        setBackground(row % 2 == 0 ? UIUtils.WHITE : UIUtils.LIGHT_GRAY);
                        setForeground(UIUtils.TEXT_DARK);
                    }
                }
                return this;
            }
        });

        // Header
        javax.swing.table.JTableHeader header = resultTable.getTableHeader();
        header.setFont(UIUtils.HEADER_FONT);
        header.setBackground(UIUtils.DARK_BLUE);
        header.setForeground(UIUtils.WHITE);
        header.setPreferredSize(new Dimension(100, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setBackground(UIUtils.DARK_BLUE);
        hr.setForeground(UIUtils.WHITE);
        hr.setFont(UIUtils.HEADER_FONT);
        hr.setBorder(new EmptyBorder(0, 12, 0, 12));
        header.setDefaultRenderer(hr);

        // Column widths
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(150);
    }

    private JLabel buildStatCard(JPanel container, String title, String value, Color accent) {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 2));
        card.setBackground(UIUtils.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                        new EmptyBorder(12, 14, 12, 14))));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.SMALL_FONT);
        titleLbl.setForeground(UIUtils.TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accent);

        JLabel subLbl = new JLabel("Current result");
        subLbl.setFont(UIUtils.SMALL_FONT);
        subLbl.setForeground(UIUtils.TEXT_MUTED);

        card.add(titleLbl);
        card.add(valueLbl);
        card.add(subLbl);
        container.add(card);
        return valueLbl;
    }
}
