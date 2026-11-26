package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class Theme {

    // ===================== COLOR PALETTE =====================
    // Primary: Navy Blue (#0F172A)
    public static final Color PRIMARY_NAVY = new Color(15, 23, 42);      // #0F172A
    public static final Color PRIMARY_NAVY_LIGHT = new Color(30, 41, 59); // #1E293B

    // Background: Light Gray (#F1F5F9)
    public static final Color BACKGROUND = new Color(241, 245, 249);     // #F1F5F9

    // Cards and panels: White (#FFFFFF)
    public static final Color CARD_WHITE = Color.WHITE;                  // #FFFFFF

    // Primary buttons: Blue (#2563EB)
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);     // #2563EB
    public static final Color PRIMARY_BLUE_HOVER = new Color(29, 78, 216); // darker blue

    // Status colors
    public static final Color STATUS_GREEN = new Color(22, 163, 74);     // #16A34A  (Available)
    public static final Color STATUS_RED = new Color(220, 38, 38);       // #DC2626  (Occupied)
    public static final Color STATUS_ORANGE = new Color(234, 88, 12);    // #EA580C  (Reserved)

    // Text colors
    public static final Color TEXT_DARK = new Color(30, 41, 59);         // #1E293B
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // #64748B
    public static final Color TEXT_WHITE = Color.WHITE;

    // Borders
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);   // #E2E8F0
    public static final Color BORDER_MEDIUM = new Color(203, 213, 225);  // #CBD5E1

    // ===================== FONTS =====================
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);

    // ===================== BACKWARD-COMPATIBLE ALIASES =====================
    // These keep existing references working while we migrate to the new names.
    public static final Color SIDEBAR = PRIMARY_NAVY;
    public static final Color ORANGE = STATUS_ORANGE;

    // ===================== BUTTON HELPERS =====================

    /**
     * Creates a modern primary button with blue background, white text,
     * no focus border, and a hand cursor.
     */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a secondary (light gray) button for less important actions.
     */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BORDER_LIGHT);
        btn.setForeground(TEXT_DARK);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a white card panel with a subtle light border.
     */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        return panel;
    }

    /**
     * Adds a hover effect that darkens the button background on mouse enter
     * and restores it on mouse exit.
     */
    public static void addHoverEffect(JButton btn) {
        final Color original = btn.getBackground();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(darken(original, 15));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });
    }

    private static Color darken(Color color, int amount) {
        int r = Math.max(0, color.getRed() - amount);
        int g = Math.max(0, color.getGreen() - amount);
        int b = Math.max(0, color.getBlue() - amount);
        return new Color(r, g, b);
    }

    // ===================== STATUS BADGES =====================

    /**
     * Creates a colored status badge (pill) for slot/reservation status.
     * Light background with colored text for a clean, modern look.
     */
    public static JLabel statusBadge(String status) {
        if (status == null) status = "-";
        JLabel badge = new JLabel(status, SwingConstants.CENTER);
        badge.setFont(Theme.FONT_LABEL_BOLD);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));

        Color bg;
        Color fg;
        String lower = status.toLowerCase();
        switch (lower) {
            case "available":
                bg = new Color(22, 163, 74, 40);
                fg = STATUS_GREEN;
                break;
            case "occupied":
                bg = new Color(220, 38, 38, 40);
                fg = STATUS_RED;
                break;
            case "reserved":
                bg = new Color(234, 88, 12, 40);
                fg = STATUS_ORANGE;
                break;
            case "pending":
                bg = new Color(37, 99, 235, 40);
                fg = PRIMARY_BLUE;
                break;
            case "approved":
                bg = new Color(22, 163, 74, 40);
                fg = STATUS_GREEN;
                break;
            case "rejected":
                bg = new Color(220, 38, 38, 40);
                fg = STATUS_RED;
                break;
            case "completed":
            case "cancelled":
                bg = BORDER_LIGHT;
                fg = TEXT_MUTED;
                break;
            default:
                bg = BORDER_LIGHT;
                fg = TEXT_MUTED;
                break;
        }
        badge.setBackground(bg);
        badge.setForeground(fg);
        return badge;
    }

    // ===================== STATISTICS CARDS =====================

    /**
     * Creates a statistics overview card with a title, large value, and accent color bar.
     */
    public static JPanel statCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(4, 0));
        card.add(accentBar, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_LABEL);
        titleLabel.setForeground(Theme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(Theme.TEXT_DARK);

        content.add(titleLabel, BorderLayout.NORTH);
        content.add(Box.createVerticalStrut(6));
        content.add(valueLabel, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ===================== SECTION HEADERS =====================

    /**
     * Creates a section header with a bold title and muted subtitle.
     */
    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Theme.TEXT_DARK);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        return header;
    }

    // ===================== SIDEBAR NAVIGATION ITEMS =====================

    /**
     * Creates a sidebar navigation item.
     * When active, shows blue background with white text and left indicator.
     * When inactive, shows navy background with muted text and hover effect.
     */
    public static JPanel sidebarItem(String label, boolean active) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(true);
        item.setBorder(new EmptyBorder(12, 20, 12, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (active) {
            item.setBackground(PRIMARY_BLUE);
        } else {
            item.setBackground(PRIMARY_NAVY);
        }

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);

        if (active) {
            JPanel indicator = new JPanel();
            indicator.setBackground(TEXT_WHITE);
            indicator.setPreferredSize(new Dimension(3, 24));
            item.add(indicator, BorderLayout.WEST);
        }

        item.add(lbl, BorderLayout.CENTER);

        if (!active) {
            item.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    item.setBackground(PRIMARY_NAVY_LIGHT);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    item.setBackground(PRIMARY_NAVY);
                }
            });
        }

        return item;
    }

    // ===================== STYLED INPUT FIELDS =====================

    /**
     * Applies modern styling to an existing JTextField.
     */
    public static void styleInput(JTextField field) {
        field.setFont(Theme.FONT_LABEL);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    /**
     * Applies modern styling to an existing JPasswordField.
     */
    public static void stylePassword(JPasswordField field) {
        field.setFont(Theme.FONT_LABEL);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    // ===================== ROUNDED PANELS =====================

    /**
     * Creates a white card panel with a subtle border and inner padding.
     */
    public static JPanel roundedPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }

    // ===================== TOAST NOTIFICATIONS =====================

    /**
     * Shows a non-blocking toast notification that auto-dismisses after 3 seconds.
     */
    public static void showToast(Component parent, String message) {
        JWindow toast = new JWindow();
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel toastPanel = new JPanel(new BorderLayout());
        toastPanel.setBackground(new Color(30, 41, 59));
        toastPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(Theme.FONT_LABEL);
        msgLabel.setForeground(Color.WHITE);
        toastPanel.add(msgLabel, BorderLayout.CENTER);

        toast.add(toastPanel);
        toast.pack();

        if (parent != null) {
            int x = parent.getX() + (parent.getWidth() - toast.getWidth()) / 2;
            int y = parent.getY() + parent.getHeight() - toast.getHeight() - 40;
            toast.setLocation(x, y);
        }

        toast.setVisible(true);

        Timer timer = new Timer(3000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
