package ui;

import db.DatabaseConnection;
import model.UserSession;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {

    private JComboBox<String> roleComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPage() {
        setTitle("Student Result Management System");
        setSize(440, 530);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        //
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.LIGHT_GRAY);

        //
        JPanel banner = new JPanel(new GridLayout(2, 1, 0, 4));
        banner.setBackground(UIUtils.DARK_BLUE);
        banner.setPreferredSize(new Dimension(0, 90));
        banner.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        JLabel appTitle = new JLabel("Result Management System");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(UIUtils.WHITE);

        JLabel appSub = new JLabel("Academic Management Portal");
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appSub.setForeground(new Color(186, 206, 255));

        banner.add(appTitle);
        banner.add(appSub);
        root.add(banner, BorderLayout.NORTH);

        //
        JPanel cardWrap = new JPanel(new GridBagLayout());
        cardWrap.setBackground(UIUtils.LIGHT_GRAY);
        cardWrap.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtils.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sign in heading
        JLabel signInLbl = new JLabel("Sign In");
        signInLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        signInLbl.setForeground(UIUtils.DARK_BLUE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(signInLbl, gbc);

        JLabel subLbl = new JLabel("Enter your credentials to access the portal");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(UIUtils.TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 6, 18, 6);
        card.add(subLbl, gbc);
        gbc.insets = new Insets(7, 6, 7, 6);

        // Role
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 2;
        JLabel roleLabel = new JLabel("Login As");
        UIUtils.styleLabel(roleLabel);
        card.add(roleLabel, gbc);

        roleComboBox = new JComboBox<>(new String[]{"Admin", "Faculty", "Student"});
        roleComboBox.setFont(UIUtils.NORMAL_FONT);
        roleComboBox.setBackground(UIUtils.WHITE);
        roleComboBox.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        gbc.gridx = 1;
        card.add(roleComboBox, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel userLabel = new JLabel("Username / ID");
        UIUtils.styleLabel(userLabel);
        card.add(userLabel, gbc);
        usernameField = new JTextField(15);
        UIUtils.styleTextField(usernameField);
        gbc.gridx = 1;
        card.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel passLabel = new JLabel("Password");
        UIUtils.styleLabel(passLabel);
        card.add(passLabel, gbc);
        passwordField = new JPasswordField(15);
        UIUtils.styleTextField(passwordField);
        gbc.gridx = 1;
        card.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 6, 6, 6);
        JButton loginButton = new JButton("Sign In");
        loginButton.setPreferredSize(new Dimension(280, 38));
        UIUtils.styleButton(loginButton);
        card.add(loginButton, gbc);

        // Role badges row
        gbc.gridy = 6;
        gbc.insets = new Insets(14, 6, 0, 6);
        JPanel badges = new JPanel(new GridLayout(1, 3, 6, 0));
        badges.setOpaque(false);
        badges.add(makeBadge("Admin",   UIUtils.DARK_BLUE));
        badges.add(makeBadge("Faculty", UIUtils.PRIMARY_BLUE));
        badges.add(makeBadge("Student", UIUtils.SUCCESS_GREEN));
        card.add(badges, gbc);

        cardWrap.add(card);
        root.add(cardWrap, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UIUtils.LIGHT_GRAY);
        JLabel footerLbl = new JLabel("Academic Management System  |  v1.0");
        footerLbl.setFont(UIUtils.SMALL_FONT);
        footerLbl.setForeground(UIUtils.TEXT_MUTED);
        footer.add(footerLbl);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
    }

    private JLabel makeBadge(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UIUtils.WHITE);
        lbl.setBackground(color);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return lbl;
    }

    private void performLogin() {
        String role     = (String) roleComboBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both credentials.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean ok = false;

            if ("Admin".equals(role)) {
                try (PreparedStatement p = conn.prepareStatement(
                        "SELECT admin_id FROM admin WHERE username = ? AND password = ?")) {
                    p.setString(1, username); p.setString(2, password);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) {
                        UserSession.setRole("ADMIN");
                        UserSession.setLoggedInUserId(rs.getString("admin_id"));
                        UserSession.setLoggedInUserName(username);
                        ok = true;
                    }
                }
            } else if ("Faculty".equals(role)) {
                try (PreparedStatement p = conn.prepareStatement(
                        "SELECT faculty_id, name FROM faculty WHERE username = ? AND password = ?")) {
                    p.setString(1, username); p.setString(2, password);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) {
                        UserSession.setRole("FACULTY");
                        UserSession.setLoggedInUserId(rs.getString("faculty_id"));
                        UserSession.setLoggedInUserName(rs.getString("name"));
                        ok = true;
                    }
                }
            } else {
                try (PreparedStatement p = conn.prepareStatement(
                        "SELECT roll_no, name FROM students WHERE roll_no = ? AND password = ?")) {
                    p.setString(1, username); p.setString(2, password);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) {
                        UserSession.setRole("STUDENT");
                        UserSession.setLoggedInUserId(rs.getString("roll_no"));
                        UserSession.setLoggedInUserName(rs.getString("name"));
                        ok = true;
                    }
                }
            }

            if (ok) {
                this.dispose();
                String r = UserSession.getRole();
                if ("ADMIN".equals(r))        new AdminModule().setVisible(true);
                else if ("FACULTY".equals(r)) new FacultyModule().setVisible(true);
                else                          new StudentModule().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or role.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
