package ui;

import db.DatabaseConnection;
import model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminModule extends JFrame {

    // Stat value labels — updated after DB load
    private JLabel statStudents;
    private JLabel statFaculty;
    private JLabel statSubjects;
    private JLabel statResults;

    public AdminModule() {
        setTitle("Admin Dashboard - Result Management System");
        setSize(1020, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.LIGHT_GRAY);

        // Top navbar
        add(UIUtils.createNavbar("Admin Dashboard", "Administrator", () -> {
            UserSession.clear();
            this.dispose();
            new LoginPage().setVisible(true);
        }), BorderLayout.NORTH);

        // Main content area
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(UIUtils.LIGHT_GRAY);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dashboard stat-cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 110));

        statStudents = UIUtils.createStatCard(statsRow, "Total Students", "0", UIUtils.PRIMARY_BLUE);
        statFaculty = UIUtils.createStatCard(statsRow, "Total Faculty", "0", UIUtils.DARK_BLUE);
        statSubjects = UIUtils.createStatCard(statsRow, "Total Subjects", "0", UIUtils.SUCCESS_GREEN);
        statResults = UIUtils.createStatCard(statsRow, "Results Uploaded", "0", UIUtils.WARN_AMBER);

        content.add(statsRow, BorderLayout.NORTH);
        loadDashboardStats();

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.HEADER_FONT);
        tabs.setBackground(UIUtils.WHITE);
        tabs.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        tabs.addTab("Manage Faculty", createManageFacultyPanel());
        tabs.addTab("Map Faculty Subjects", createMapFacultySubjectsPanel());
        tabs.addTab("Manage Students", createManageStudentsPanel());

        tabs.setForegroundAt(0, UIUtils.DARK_BLUE);
        tabs.setForegroundAt(1, UIUtils.DARK_BLUE);
        tabs.setForegroundAt(2, UIUtils.DARK_BLUE);

        content.add(tabs, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // =========================================================================
    // DASHBOARD STATS
    // =========================================================================
    private void loadDashboardStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            statStudents.setText(queryCount(conn, "SELECT COUNT(*) FROM students"));
            statFaculty.setText(queryCount(conn, "SELECT COUNT(*) FROM faculty"));
            statSubjects.setText(queryCount(conn, "SELECT COUNT(*) FROM subjects"));
            statResults.setText(queryCount(conn, "SELECT COUNT(*) FROM results"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String queryCount(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? String.valueOf(rs.getInt(1)) : "0";
        }
    }

    // =========================================================================
    // TAB 1 – Manage Faculty
    // =========================================================================
    private JPanel createManageFacultyPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Form card
        JPanel formCard = new JPanel(new GridLayout(5, 2, 10, 10));
        UIUtils.applyColoredBorder(formCard, " Manage Faculty ");

        JTextField idField = new JTextField();
        UIUtils.styleTextField(idField);
        JTextField nameField = new JTextField();
        UIUtils.styleTextField(nameField);
        JTextField userField = new JTextField();
        UIUtils.styleTextField(userField);
        JPasswordField passField = new JPasswordField();
        UIUtils.styleTextField(passField);

        addLF(formCard, "Faculty ID:", idField);
        addLF(formCard, "Name:", nameField);
        addLF(formCard, "Username:", userField);
        addLF(formCard, "Password:", passField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        JButton addBtn = new JButton("Add Faculty");
        UIUtils.styleButton(addBtn);
        JButton delBtn = new JButton("Delete Faculty");
        UIUtils.styleDangerButton(delBtn);
        btnRow.add(addBtn);
        btnRow.add(delBtn);
        formCard.add(new JLabel());
        formCard.add(btnRow);

        // Table
        DefaultTableModel tm = new DefaultTableModel(
                new String[] { "Faculty ID", "Name", "Username" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tm);
        UIUtils.styleTable(table);
        loadFaculty(tm);

        // Add action
        addBtn.addActionListener(e -> {
            try (Connection c = DatabaseConnection.getConnection();
                    PreparedStatement p = c.prepareStatement(
                            "INSERT INTO faculty (faculty_id, name, username, password) VALUES (?, ?, ?, ?)")) {
                p.setString(1, idField.getText().trim());
                p.setString(2, nameField.getText().trim());
                p.setString(3, userField.getText().trim());
                p.setString(4, new String(passField.getPassword()));
                p.executeUpdate();
                JOptionPane.showMessageDialog(this, "Faculty added successfully.");
                loadFaculty(tm);
                loadDashboardStats();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Delete action
        delBtn.addActionListener(e -> {
            String facId = idField.getText().trim();
            if (facId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the Faculty ID to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete faculty ID " + facId + "? This will also remove their subject assignments.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION)
                return;

            try (Connection c = DatabaseConnection.getConnection()) {
                // Remove FK mappings first
                try (PreparedStatement p1 = c.prepareStatement(
                        "DELETE FROM faculty_subject_mapping WHERE faculty_id=?")) {
                    p1.setString(1, facId);
                    p1.executeUpdate();
                }
                // Delete faculty
                try (PreparedStatement p2 = c.prepareStatement(
                        "DELETE FROM faculty WHERE faculty_id=?")) {
                    p2.setString(1, facId);
                    int rows = p2.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Faculty deleted successfully.");
                        loadFaculty(tm);
                        loadDashboardStats();
                    } else {
                        JOptionPane.showMessageDialog(this, "Faculty ID not found.",
                                "Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JScrollPane scroll = styledScroll(table);
        panel.add(formCard, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadFaculty(DefaultTableModel m) {
        m.setRowCount(0);
        try (Connection c = DatabaseConnection.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT faculty_id, name, username FROM faculty")) {
            while (rs.next())
                m.addRow(new Object[] { rs.getString("faculty_id"), rs.getString("name"), rs.getString("username") });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // TAB 2 – Map Faculty → Subjects
    // =========================================================================
    private JPanel createMapFacultySubjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel formCard = new JPanel(new GridLayout(3, 2, 10, 10));
        UIUtils.applyColoredBorder(formCard, " Assign Subject to Faculty ");

        JComboBox<String> facultyCombo = styledCombo();
        JComboBox<String> subjectCombo = styledCombo();
        subjectCombo.setPreferredSize(new Dimension(240, 30));

        try (Connection c = DatabaseConnection.getConnection()) {
            try (Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery("SELECT faculty_id FROM faculty")) {
                while (rs.next())
                    facultyCombo.addItem(rs.getString("faculty_id"));
            }
            try (Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery("SELECT subject_id, subject_name FROM subjects")) {
                while (rs.next())
                    subjectCombo.addItem(rs.getString("subject_id") + " - " + rs.getString("subject_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addLC(formCard, "Select Faculty ID:", facultyCombo);
        addLC(formCard, "Select Subject:", subjectCombo);

        JButton assignBtn = new JButton("Assign Subject");
        UIUtils.styleButton(assignBtn);
        formCard.add(new JLabel());
        formCard.add(assignBtn);

        assignBtn.addActionListener(e -> {
            String fId = (String) facultyCombo.getSelectedItem();
            String sFull = (String) subjectCombo.getSelectedItem();
            if (fId != null && sFull != null) {
                String sId = sFull.split(" - ")[0];
                try (Connection c = DatabaseConnection.getConnection();
                        PreparedStatement p = c.prepareStatement(
                                "INSERT INTO faculty_subject_mapping (faculty_id, subject_id) VALUES (?, ?)")) {
                    p.setString(1, fId);
                    p.setString(2, sId);
                    p.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Subject assigned successfully.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(formCard, BorderLayout.NORTH);
        return panel;
    }

    // =========================================================================
    // TAB 3 – Manage Students
    // =========================================================================
    private JPanel createManageStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel formCard = new JPanel(new GridLayout(6, 2, 10, 10));
        UIUtils.applyColoredBorder(formCard, " Manage Students ");

        JTextField rollField = new JTextField();
        UIUtils.styleTextField(rollField);
        JTextField nameField = new JTextField();
        UIUtils.styleTextField(nameField);
        JTextField semField = new JTextField();
        UIUtils.styleTextField(semField);
        JTextField deptField = new JTextField();
        UIUtils.styleTextField(deptField);
        JPasswordField passField = new JPasswordField();
        UIUtils.styleTextField(passField);

        addLF(formCard, "Roll No:", rollField);
        addLF(formCard, "Name:", nameField);
        addLF(formCard, "Semester:", semField);
        addLF(formCard, "Department:", deptField);
        addLF(formCard, "Password:", passField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        JButton addBtn = new JButton("Add");
        UIUtils.styleButton(addBtn);
        JButton updateBtn = new JButton("Update");
        UIUtils.styleWarnButton(updateBtn);
        JButton delBtn = new JButton("Delete");
        UIUtils.styleDangerButton(delBtn);
        btnRow.add(addBtn);
        btnRow.add(updateBtn);
        btnRow.add(delBtn);
        formCard.add(new JLabel());
        formCard.add(btnRow);

        DefaultTableModel tm = new DefaultTableModel(
                new String[] { "Roll No", "Name", "Semester", "Department" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tm);
        UIUtils.styleTable(table);

        // Click row → auto-fill form
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                rollField.setText(tm.getValueAt(r, 0).toString());
                nameField.setText(tm.getValueAt(r, 1).toString());
                semField.setText(tm.getValueAt(r, 2).toString());
                deptField.setText(tm.getValueAt(r, 3).toString());
            }
        });

        loadStudents(tm);

        addBtn.addActionListener(e -> {
            try (Connection c = DatabaseConnection.getConnection();
                    PreparedStatement p = c.prepareStatement(
                            "INSERT INTO students (roll_no, name, semester, department, password) VALUES (?, ?, ?, ?, ?)")) {
                p.setString(1, rollField.getText().trim());
                p.setString(2, nameField.getText().trim());
                p.setInt(3, Integer.parseInt(semField.getText().trim()));
                p.setString(4, deptField.getText().trim());
                p.setString(5, new String(passField.getPassword()));
                p.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student added successfully.");
                loadStudents(tm);
                loadDashboardStats();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        updateBtn.addActionListener(e -> {
            try (Connection c = DatabaseConnection.getConnection();
                    PreparedStatement p = c.prepareStatement(
                            "UPDATE students SET name=?, semester=?, department=? WHERE roll_no=?")) {
                p.setString(1, nameField.getText().trim());
                p.setInt(2, Integer.parseInt(semField.getText().trim()));
                p.setString(3, deptField.getText().trim());
                p.setString(4, rollField.getText().trim());
                if (p.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Student updated.");
                    loadStudents(tm);
                } else
                    JOptionPane.showMessageDialog(this, "Student not found.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        delBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete student with Roll No: " + rollField.getText().trim() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION)
                return;
            try (Connection c = DatabaseConnection.getConnection();
                    PreparedStatement p = c.prepareStatement("DELETE FROM students WHERE roll_no=?")) {
                p.setString(1, rollField.getText().trim());
                if (p.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Student deleted.");
                    loadStudents(tm);
                    loadDashboardStats();
                } else
                    JOptionPane.showMessageDialog(this, "Student not found.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(styledScroll(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadStudents(DefaultTableModel m) {
        m.setRowCount(0);
        try (Connection c = DatabaseConnection.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT roll_no, name, semester, department FROM students")) {
            while (rs.next())
                m.addRow(new Object[] { rs.getString("roll_no"), rs.getString("name"),
                        rs.getInt("semester"), rs.getString("department") });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void addLF(JPanel p, String txt, JTextField f) {
        JLabel l = new JLabel(txt);
        UIUtils.styleLabel(l);
        p.add(l);
        p.add(f);
    }

    private void addLC(JPanel p, String txt, JComboBox<String> c) {
        JLabel l = new JLabel(txt);
        UIUtils.styleLabel(l);
        p.add(l);
        p.add(c);
    }

    private JComboBox<String> styledCombo() {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(UIUtils.NORMAL_FONT);
        c.setBackground(UIUtils.WHITE);
        return c;
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        sp.getViewport().setBackground(UIUtils.WHITE);
        return sp;
    }
}