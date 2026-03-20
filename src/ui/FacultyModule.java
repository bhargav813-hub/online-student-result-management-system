package ui;

import db.DatabaseConnection;
import model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class FacultyModule extends JFrame {

    private int selectedSemester = -1;
    private String selectedSubjectId = null;

    public FacultyModule() {
        setTitle("Faculty Dashboard - Result Management System");
        setSize(1020, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.LIGHT_GRAY);

        add(UIUtils.createNavbar(
                "Faculty Dashboard",
                UserSession.getLoggedInUserName(),
                () -> {
                    model.UserSession.clear();
                    this.dispose();
                    new LoginPage().setVisible(true);
                }), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UIUtils.LIGHT_GRAY);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.HEADER_FONT);
        tabs.setBackground(UIUtils.WHITE);
        tabs.addTab("Enter / Update Marks", buildMarksTab());
        tabs.addTab("My Subjects", buildMySubjectsTab());
        tabs.addTab("Change Password", buildChangePasswordTab());
        tabs.setForegroundAt(0, UIUtils.DARK_BLUE);
        tabs.setForegroundAt(1, UIUtils.DARK_BLUE);
        tabs.setForegroundAt(2, UIUtils.DARK_BLUE);

        content.add(tabs, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // =========================================================================
    // TAB 1 - Enter / Update Marks
    // =========================================================================
    private JPanel buildMarksTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UIUtils.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Selector row
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        selectorRow.setBackground(UIUtils.LIGHT_GRAY);
        selectorRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                " Filter ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                UIUtils.HEADER_FONT, UIUtils.DARK_BLUE));

        JComboBox<String> semCombo = new JComboBox<>();
        JComboBox<String> subjectCombo = new JComboBox<>();
        semCombo.setPreferredSize(new Dimension(130, 30));
        subjectCombo.setPreferredSize(new Dimension(220, 30));
        semCombo.setFont(UIUtils.NORMAL_FONT);
        subjectCombo.setFont(UIUtils.NORMAL_FONT);
        semCombo.setBackground(UIUtils.WHITE);
        subjectCombo.setBackground(UIUtils.WHITE);

        JButton refreshBtn = new JButton("Refresh");
        UIUtils.styleActionButton(refreshBtn);

        JLabel l1 = new JLabel("Semester:");
        UIUtils.styleLabel(l1);
        JLabel l2 = new JLabel("Subject:");
        UIUtils.styleLabel(l2);
        selectorRow.add(l1);
        selectorRow.add(semCombo);
        selectorRow.add(l2);
        selectorRow.add(subjectCombo);
        selectorRow.add(refreshBtn);
        panel.add(selectorRow, BorderLayout.NORTH);

        // Table
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[] { "Roll No", "Student Name", "Marks", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        styleMarksTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        scroll.getViewport().setBackground(UIUtils.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Entry bar
        JPanel entryBar = new JPanel(new GridBagLayout());
        entryBar.setBackground(UIUtils.LIGHT_GRAY);
        entryBar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIUtils.BORDER_COLOR),
                new EmptyBorder(10, 15, 10, 15)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 6, 0, 6);
        gc.anchor = GridBagConstraints.WEST;

        JLabel rollLbl = new JLabel("Roll No:");
        UIUtils.styleLabel(rollLbl);
        JLabel marksLbl = new JLabel("Marks (0-100):");
        UIUtils.styleLabel(marksLbl);

        JTextField rollInput = new JTextField(12);
        UIUtils.styleTextField(rollInput);
        JTextField marksInput = new JTextField(8);
        UIUtils.styleTextField(marksInput);
        rollInput.setPreferredSize(new Dimension(140, 32));
        marksInput.setPreferredSize(new Dimension(100, 32));

        JButton saveBtn = new JButton("Save Marks");
        UIUtils.styleButton(saveBtn);
        saveBtn.setPreferredSize(new Dimension(130, 34));

        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        entryBar.add(rollLbl, gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.3;
        entryBar.add(rollInput, gc);
        gc.gridx = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        entryBar.add(marksLbl, gc);
        gc.gridx = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.2;
        entryBar.add(marksInput, gc);
        gc.gridx = 4;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        entryBar.add(saveBtn, gc);
        panel.add(entryBar, BorderLayout.SOUTH);

        // Data loading
        String facultyId = UserSession.getLoggedInUserId();
        List<Integer> assignedSems = new ArrayList<>();
        List<Object[]> currentSubjects = new ArrayList<>();

        // Load semesters this faculty teaches
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "SELECT DISTINCT s.semester FROM subjects s " +
                                "JOIN faculty_subject_mapping f ON s.subject_id = f.subject_id " +
                                "WHERE f.faculty_id = ? ORDER BY s.semester")) {
            p.setString(1, facultyId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                int sem = rs.getInt("semester");
                semCombo.addItem("Semester " + sem);
                assignedSems.add(sem);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Fallback: load all semesters if none assigned
        if (assignedSems.isEmpty()) {
            try (Connection c = DatabaseConnection.getConnection();
                    Statement st = c.createStatement();
                    ResultSet rs = st.executeQuery("SELECT DISTINCT semester FROM subjects ORDER BY semester")) {
                while (rs.next()) {
                    int sem = rs.getInt("semester");
                    semCombo.addItem("Semester " + sem);
                    assignedSems.add(sem);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        Runnable loadSubjects = () -> {
            subjectCombo.removeAllItems();
            currentSubjects.clear();
            int idx = semCombo.getSelectedIndex();
            if (idx < 0 || idx >= assignedSems.size())
                return;
            selectedSemester = assignedSems.get(idx);
            try (Connection c = DatabaseConnection.getConnection();
                    PreparedStatement p = c.prepareStatement(
                            "SELECT s.subject_id, s.subject_name FROM subjects s " +
                                    "JOIN faculty_subject_mapping f ON s.subject_id = f.subject_id " +
                                    "WHERE f.faculty_id = ? AND s.semester = ?")) {
                p.setString(1, facultyId);
                p.setInt(2, selectedSemester);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                    currentSubjects.add(new Object[] { rs.getString("subject_id"), rs.getString("subject_name") });
                    subjectCombo.addItem(rs.getString("subject_name"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

        Runnable loadStudents = () -> {
            tableModel.setRowCount(0);
            int idx = subjectCombo.getSelectedIndex();
            if (idx < 0 || idx >= currentSubjects.size())
                return;
            selectedSubjectId = currentSubjects.get(idx)[0].toString();
            try (Connection c = DatabaseConnection.getConnection();
                    PreparedStatement p = c.prepareStatement(
                            "SELECT st.roll_no, st.name, r.marks FROM students st " +
                                    "LEFT JOIN results r ON st.roll_no = r.roll_no AND r.subject_id = ? " +
                                    "WHERE st.semester = ? ORDER BY st.roll_no")) {
                p.setString(1, selectedSubjectId);
                p.setInt(2, selectedSemester);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                    Object marks = rs.getObject("marks");
                    String status = marks == null ? "Not Entered"
                            : (((Number) marks).doubleValue() >= 35 ? "Pass" : "Fail");
                    tableModel.addRow(new Object[] {
                            rs.getString("roll_no"), rs.getString("name"),
                            marks == null ? "" : marks, status });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

        refreshBtn.addActionListener(e -> loadStudents.run());

        // Click row -> autofill entry fields
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                rollInput.setText(tableModel.getValueAt(r, 0).toString());
                Object m = tableModel.getValueAt(r, 2);
                marksInput.setText(m != null ? String.valueOf(m) : "");
            }
        });

        // Save marks
        saveBtn.addActionListener(e -> {
            String roll = rollInput.getText().trim().toUpperCase();
            String marksStr = marksInput.getText().trim();
            if (roll.isEmpty() || marksStr.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "Please enter a roll number and marks value.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedSubjectId == null) {
                JOptionPane.showMessageDialog(panel,
                        "Please select a subject first.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double marks = Double.parseDouble(marksStr);
                if (marks < 0 || marks > 100)
                    throw new NumberFormatException();
                try (Connection c = DatabaseConnection.getConnection()) {
                    boolean exists;
                    try (PreparedStatement p = c.prepareStatement(
                            "SELECT 1 FROM results WHERE roll_no = ? AND subject_id = ?")) {
                        p.setString(1, roll);
                        p.setString(2, selectedSubjectId);
                        exists = p.executeQuery().next();
                    }
                    String q = exists
                            ? "UPDATE results SET marks = ? WHERE roll_no = ? AND subject_id = ?"
                            : "INSERT INTO results (marks, roll_no, subject_id) VALUES (?, ?, ?)";
                    try (PreparedStatement p = c.prepareStatement(q)) {
                        p.setDouble(1, marks);
                        p.setString(2, roll);
                        p.setString(3, selectedSubjectId);
                        p.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(panel,
                            "Marks saved successfully for " + roll + "!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadStudents.run();
                    rollInput.setText("");
                    marksInput.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Marks must be a number between 0 and 100.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        marksInput.addActionListener(e -> saveBtn.doClick());

        // Register listeners AFTER initial population to avoid premature firing during
        // addItem()
        semCombo.addActionListener(e -> {
            loadSubjects.run();
            loadStudents.run();
        });
        subjectCombo.addActionListener(e -> loadStudents.run());

        if (!assignedSems.isEmpty()) {
            loadSubjects.run();
            loadStudents.run();
        }
        return panel;
    }

    // =========================================================================
    // TAB 2 - My Subjects
    // =========================================================================
    private JPanel buildMySubjectsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        panel.setBackground(UIUtils.WHITE);

        JLabel info = new JLabel(
                "<html><i>All subjects assigned to you across all semesters.</i></html>",
                JLabel.CENTER);
        info.setForeground(UIUtils.TEXT_MUTED);
        info.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        info.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(info, BorderLayout.NORTH);

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[] { "Subject ID", "Subject Name", "Semester" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        UIUtils.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        scroll.getViewport().setBackground(UIUtils.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        String facultyId = UserSession.getLoggedInUserId();
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "SELECT s.subject_id, s.subject_name, s.semester FROM subjects s " +
                                "JOIN faculty_subject_mapping f ON s.subject_id = f.subject_id " +
                                "WHERE f.faculty_id = ? ORDER BY s.semester, s.subject_name")) {
            p.setString(1, facultyId);
            ResultSet rs = p.executeQuery();
            while (rs.next())
                tableModel.addRow(new Object[] {
                        rs.getString("subject_id"), rs.getString("subject_name"), rs.getInt("semester") });
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return panel;
    }

    // =========================================================================
    // TAB 3 - Change Password (with confirm field)
    // =========================================================================
    private JPanel buildChangePasswordTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UIUtils.LIGHT_GRAY);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtils.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                new EmptyBorder(32, 40, 32, 40)));

        GridBagConstraints gc = new GridBagConstraints();

        JLabel head = new JLabel("Change Password", SwingConstants.CENTER);
        head.setFont(new Font("Segoe UI", Font.BOLD, 18));
        head.setForeground(UIUtils.DARK_BLUE);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(0, 0, 6, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        card.add(head, gc);

        JSeparator sep = new JSeparator();
        sep.setForeground(UIUtils.BORDER_COLOR);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 18, 0);
        card.add(sep, gc);

        JPasswordField oldPass = new JPasswordField();
        oldPass.setPreferredSize(new Dimension(220, 34));
        UIUtils.styleTextField(oldPass);
        JPasswordField newPass = new JPasswordField();
        newPass.setPreferredSize(new Dimension(220, 34));
        UIUtils.styleTextField(newPass);
        JPasswordField confPass = new JPasswordField();
        confPass.setPreferredSize(new Dimension(220, 34));
        UIUtils.styleTextField(confPass);

        gc.gridwidth = 1;
        gc.insets = new Insets(9, 6, 9, 10);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.EAST;
        card.add(makeLabel("Current Password:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;
        card.add(oldPass, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.EAST;
        card.add(makeLabel("New Password:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;
        card.add(newPass, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.EAST;
        card.add(makeLabel("Confirm New Password:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;
        card.add(confPass, gc);

        JButton saveBtn = new JButton("Update Password");
        saveBtn.setFont(UIUtils.BUTTON_FONT);
        saveBtn.setBackground(UIUtils.PRIMARY_BLUE);
        saveBtn.setForeground(UIUtils.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(340, 38));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                saveBtn.setBackground(new Color(29, 78, 216));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                saveBtn.setBackground(UIUtils.PRIMARY_BLUE);
            }
        });

        gc.gridx = 0;
        gc.gridy = 5;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(22, 6, 6, 6);
        card.add(saveBtn, gc);

        saveBtn.addActionListener(e -> doChangePassword(outer, oldPass, newPass, confPass));
        confPass.addActionListener(e -> saveBtn.doClick());

        outer.add(card);
        return outer;
    }

    // =========================================================================
    // PASSWORD CHANGE LOGIC
    // =========================================================================
    private void doChangePassword(JPanel parent,
            JPasswordField oldPass,
            JPasswordField newPass,
            JPasswordField confPass) {
        String op = new String(oldPass.getPassword()).trim();
        String np = new String(newPass.getPassword()).trim();
        String cp = new String(confPass.getPassword()).trim();

        if (op.isEmpty() || np.isEmpty() || cp.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "All three password fields are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!np.equals(cp)) {
            JOptionPane.showMessageDialog(parent, "New password and confirmation do not match.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            newPass.setText("");
            confPass.setText("");
            newPass.requestFocus();
            return;
        }
        if (np.length() < 6) {
            JOptionPane.showMessageDialog(parent, "New password must be at least 6 characters.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String facultyId = UserSession.getLoggedInUserId();
        try (Connection c = DatabaseConnection.getConnection()) {
            boolean verified = false;
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT password FROM faculty WHERE faculty_id = ?")) {
                p.setString(1, facultyId);
                ResultSet rs = p.executeQuery();
                if (rs.next() && op.equals(rs.getString("password")))
                    verified = true;
            }
            if (verified) {
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE faculty SET password = ? WHERE faculty_id = ?")) {
                    p.setString(1, np);
                    p.setString(2, facultyId);
                    p.executeUpdate();
                }
                JOptionPane.showMessageDialog(parent, "Password updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                oldPass.setText("");
                newPass.setText("");
                confPass.setText("");
            } else {
                JOptionPane.showMessageDialog(parent,
                        "Current password is incorrect. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                oldPass.setText("");
                oldPass.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        UIUtils.styleLabel(l);
        return l;
    }

    private void styleMarksTable(JTable table) {
        UIUtils.styleTable(table);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setFont(UIUtils.NORMAL_FONT);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    String status = t.getModel().getValueAt(row, 3) == null ? ""
                            : t.getModel().getValueAt(row, 3).toString();
                    if ("Fail".equals(status)) {
                        setBackground(new Color(254, 242, 242));
                        setForeground(col == 3 ? new Color(185, 28, 28) : UIUtils.TEXT_DARK);
                    } else if ("Pass".equals(status)) {
                        setBackground(row % 2 == 0 ? UIUtils.WHITE : UIUtils.LIGHT_GRAY);
                        setForeground(col == 3 ? new Color(5, 150, 105) : UIUtils.TEXT_DARK);
                    } else {
                        setBackground(row % 2 == 0 ? UIUtils.WHITE : UIUtils.LIGHT_GRAY);
                        setForeground(UIUtils.TEXT_MUTED);
                    }
                }
                return this;
            }
        });
    }
}
