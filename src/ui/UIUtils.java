package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIUtils {

    //
    public static final Color PRIMARY_BLUE     = new Color(37, 99, 235);    // #2563EB
    public static final Color DARK_BLUE        = new Color(30, 58, 138);    // #1E3A8A
    public static final Color WHITE            = new Color(255, 255, 255);  // #FFFFFF
    public static final Color LIGHT_GRAY       = new Color(243, 244, 246);  // #F3F4F6
    public static final Color TEXT_DARK        = new Color(17, 24, 39);     // #111827
    public static final Color TEXT_MUTED       = new Color(107, 114, 128);  // muted gray
    public static final Color BORDER_COLOR     = new Color(209, 213, 219);  // #D1D5DB
    public static final Color SUCCESS_GREEN    = new Color(16, 185, 129);   // for success states
    public static final Color DANGER_RED       = new Color(220, 38, 38);    // for delete
    public static final Color WARN_AMBER       = new Color(217, 119, 6);    // for update

    // Aliases used by modules
    public static final Color PRIMARY_COLOR    = PRIMARY_BLUE;
    public static final Color SECONDARY_COLOR  = DARK_BLUE;
    public static final Color BACKGROUND_COLOR = WHITE;
    public static final Color CARD_BACKGROUND  = LIGHT_GRAY;
    public static final Color TEXT_COLOR       = TEXT_DARK;

    //
    public static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT  = new Font("Segoe UI", Font.PLAIN, 11);

    //
    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(WHITE);
    }

    //
    public static void styleTitleLabel(JLabel label) {
        label.setFont(TITLE_FONT);
        label.setForeground(WHITE);
        label.setBorder(new EmptyBorder(12, 0, 12, 0));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public static void styleLabel(JLabel label) {
        label.setFont(NORMAL_FONT);
        label.setForeground(TEXT_DARK);
    }

    //
    public static void styleTextField(JTextField field) {
        field.setFont(NORMAL_FONT);
        field.setBackground(WHITE);
        field.setForeground(TEXT_DARK);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        field.setCaretColor(PRIMARY_BLUE);
    }

    //
    public static void styleButton(JButton button) {
        applyBtn(button, PRIMARY_BLUE, new Color(29, 78, 216));
    }

    public static void styleActionButton(JButton button) {
        applyBtn(button, PRIMARY_BLUE, new Color(29, 78, 216));
        button.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
    }

    public static void styleDangerButton(JButton button) {
        applyBtn(button, DANGER_RED, new Color(185, 28, 28));
    }

    public static void styleWarnButton(JButton button) {
        applyBtn(button, WARN_AMBER, new Color(180, 90, 0));
    }

    public static void styleOutlineButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(WHITE);
        button.setForeground(PRIMARY_BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(239, 246, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(WHITE);
            }
        });
    }

    private static void applyBtn(JButton button, Color normal, Color hover) {
        button.setFont(BUTTON_FONT);
        button.setBackground(normal);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { button.setBackground(normal); }
        });
    }

    //
    public static void styleTable(JTable table) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(34);
        table.setBackground(WHITE);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected)
                    setBackground(row % 2 == 0 ? WHITE : LIGHT_GRAY);
                setForeground(TEXT_DARK);
                setFont(NORMAL_FONT);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(DARK_BLUE);
        header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(100, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setHorizontalAlignment(JLabel.LEFT);
        hr.setBackground(DARK_BLUE);
        hr.setForeground(WHITE);
        hr.setFont(HEADER_FONT);
        hr.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        header.setDefaultRenderer(hr);
    }

    //
    /**
     * Creates the top navigation bar: dark blue background.
     * Pass the page title string and the logout action.
     */
    public static JPanel createNavbar(String pageTitle, String userName, Runnable onLogout) {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(DARK_BLUE);
        navbar.setPreferredSize(new Dimension(0, 58));
        navbar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Left: Logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JLabel logoBox = new JLabel("AMS");
        logoBox.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoBox.setForeground(DARK_BLUE);
        logoBox.setBackground(WHITE);
        logoBox.setOpaque(true);
        logoBox.setBorder(BorderFactory.createEmptyBorder(4, 9, 4, 9));

        JLabel appName = new JLabel("Result Management System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appName.setForeground(WHITE);

        JSeparator vsep = new JSeparator(SwingConstants.VERTICAL);
        vsep.setPreferredSize(new Dimension(1, 22));
        vsep.setForeground(new Color(99, 130, 200));

        JLabel pageLbl = new JLabel(pageTitle);
        pageLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pageLbl.setForeground(new Color(186, 206, 255));

        left.add(logoBox);
        left.add(appName);
        left.add(vsep);
        left.add(pageLbl);

        // Right: user + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel userLbl = new JLabel(userName);
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLbl.setForeground(new Color(186, 206, 255));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setForeground(DARK_BLUE);
        logoutBtn.setBackground(WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> onLogout.run());
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(LIGHT_GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(WHITE);
            }
        });

        right.add(userLbl);
        right.add(logoutBtn);

        navbar.add(left, BorderLayout.CENTER);
        navbar.add(right, BorderLayout.EAST);

        // Vertical centering trick
        navbar.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int h = navbar.getHeight();
                left.setBorder(BorderFactory.createEmptyBorder((h - 28) / 2, 0, 0, 0));
                right.setBorder(BorderFactory.createEmptyBorder((h - 28) / 2, 0, 0, 0));
            }
        });

        return navbar;
    }

    //
    public static JPanel createSectionHeader(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_BLUE),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(HEADER_FONT);
        lbl.setForeground(DARK_BLUE);
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    //
    public static void applyColoredBorder(JPanel panel, String title) {
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                HEADER_FONT,
                DARK_BLUE));
    }

    //
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        return panel;
    }

    //
    /**
     * Creates a dashboard stat card with a title, a large value label (returned),
     * and an accent left border in the given color.
     */
    public static JLabel createStatCard(JPanel container, String title, String value, Color accent) {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 4));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16))));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(SMALL_FONT);
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(accent);

        JLabel subLbl = new JLabel("Total registered");
        subLbl.setFont(SMALL_FONT);
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl);
        card.add(valueLbl);
        card.add(subLbl);
        container.add(card);
        return valueLbl;
    }
}
