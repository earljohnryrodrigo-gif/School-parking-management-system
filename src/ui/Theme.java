package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/** Shared colors, typography, and reusable Swing components. */
public final class Theme {
    private Theme() { }

    public static final Color PRIMARY_NAVY = new Color(15, 23, 42);
    public static final Color PRIMARY_NAVY_LIGHT = new Color(30, 41, 59);
    public static final Color BACKGROUND = new Color(241, 245, 249);
    public static final Color CARD_WHITE = Color.WHITE;
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    public static final Color PRIMARY_BLUE_HOVER = new Color(29, 78, 216);
    public static final Color STATUS_GREEN = new Color(22, 163, 74);
    public static final Color STATUS_RED = new Color(220, 38, 38);
    public static final Color STATUS_ORANGE = new Color(234, 88, 12);
    public static final Color TEXT_DARK = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color TEXT_WHITE = Color.WHITE;
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);
    public static final Color BORDER_MEDIUM = new Color(203, 213, 225);
    public static final Color SIDEBAR = PRIMARY_NAVY;
    public static final Color ORANGE = STATUS_ORANGE;

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 23);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);

    public static void installDefaults() {
        UIManager.put("OptionPane.background", CARD_WHITE);
        UIManager.put("Panel.background", CARD_WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT_DARK);
        UIManager.put("OptionPane.messageFont", FONT_LABEL);
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Button.background", CARD_WHITE);
        UIManager.put("Button.foreground", TEXT_DARK);
        UIManager.put("Label.font", FONT_LABEL);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("TextField.font", FONT_LABEL);
        UIManager.put("PasswordField.font", FONT_LABEL);
        UIManager.put("ComboBox.font", FONT_LABEL);
        UIManager.put("CheckBox.font", FONT_LABEL);
        UIManager.put("CheckBox.foreground", TEXT_DARK);
        UIManager.put("Table.font", FONT_TABLE_CELL);
        UIManager.put("TableHeader.font", FONT_TABLE_HEADER);
        UIManager.put("TabbedPane.font", FONT_LABEL_BOLD);
        UIManager.put("TabbedPane.selected", CARD_WHITE);
        UIManager.put("TabbedPane.contentAreaColor", CARD_WHITE);
        UIManager.put("TabbedPane.borderHightlightColor", BORDER_LIGHT);
        UIManager.put("TabbedPane.darkShadow", BORDER_LIGHT);
        UIManager.put("TabbedPane.light", CARD_WHITE);
    }

    public static JButton primaryButton(String text) { return button(text, PRIMARY_BLUE, TEXT_WHITE, PRIMARY_BLUE_HOVER); }
    public static JButton secondaryButton(String text) { return button(text, CARD_WHITE, TEXT_DARK, new Color(241, 245, 249)); }

    public static JButton iconButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setPreferredSize(new Dimension(42, 42));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setBackground(CARD_WHITE);
        button.setForeground(TEXT_DARK);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        addHoverEffect(button);
        return button;
    }

    private static JButton button(String text, Color background, Color foreground, Color hover) {
        JButton button = new RoundedButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(9, 16, 9, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setRolloverEnabled(true);
        button.putClientProperty("theme.hover", hover);
        button.setPreferredSize(new Dimension(Math.max(100, button.getPreferredSize().width), 38));
        return button;
    }

    private static final class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        RoundedButton(String text) { super(text); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getBackground();
            if (!isEnabled()) fill = new Color(203, 213, 225);
            else if (getModel().isPressed()) fill = darken(fill, 28);
            else if (getModel().isRollover()) fill = (Color) getClientProperty("theme.hover");
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    public static JPanel cardPanel() {
        JPanel panel = new RoundedCardPanel();
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        return panel;
    }

    private static final class RoundedCardPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(15, 23, 42, 14));
            g.fillRoundRect(1, 2, getWidth() - 3, getHeight() - 3, 17, 17);
            g.setColor(CARD_WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setColor(BORDER_LIGHT);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.dispose();
        }
    }

    public static JPanel roundedPanel() { return cardPanel(); }

    public static void addHoverEffect(JButton button) {
        final Color original = button.getBackground();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled() && button.getClientProperty("theme.hover") == null) button.setBackground(darken(original, 12));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.getClientProperty("theme.hover") == null) button.setBackground(original);
            }
        });
    }

    private static Color darken(Color color, int amount) {
        if (color == null) return PRIMARY_BLUE_HOVER;
        return new Color(Math.max(0, color.getRed() - amount), Math.max(0, color.getGreen() - amount), Math.max(0, color.getBlue() - amount));
    }

    public static JLabel statusBadge(String status) {
        if (status == null) status = "-";
        JLabel badge = new JLabel(status, SwingConstants.CENTER);
        badge.setFont(FONT_LABEL_BOLD);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        Color fg = TEXT_MUTED, bg = BORDER_LIGHT;
        switch (status.toLowerCase()) {
            case "available": case "approved": fg = STATUS_GREEN; bg = new Color(220, 252, 231); break;
            case "occupied": case "rejected": fg = STATUS_RED; bg = new Color(254, 226, 226); break;
            case "reserved": fg = STATUS_ORANGE; bg = new Color(255, 237, 213); break;
            case "pending": fg = PRIMARY_BLUE; bg = new Color(219, 234, 254); break;
            default: break;
        }
        badge.setForeground(fg); badge.setBackground(bg);
        return badge;
    }

    public static JPanel statCard(String title, String value, Color accent) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(14, 0));
        JPanel accentBar = new JPanel(); accentBar.setBackground(accent); accentBar.setPreferredSize(new Dimension(4, 0));
        JPanel content = new JPanel(new BorderLayout(0, 5)); content.setOpaque(false);
        JLabel label = new JLabel(statIcon(title) + "  " + title); label.setFont(FONT_LABEL); label.setForeground(TEXT_MUTED);
        JLabel number = new JLabel(value); number.setFont(new Font("Segoe UI", Font.BOLD, 25)); number.setForeground(TEXT_DARK);
        content.add(label, BorderLayout.NORTH); content.add(number, BorderLayout.CENTER);
        card.add(accentBar, BorderLayout.WEST); card.add(content, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(180, 82));
        return card;
    }

    private static String statIcon(String title) {
        String value = title == null ? "" : title.toLowerCase(java.util.Locale.ROOT);
        if (value.contains("available")) return "✅";
        if (value.contains("occupied")) return "🚗";
        if (value.contains("reserved") || value.contains("request")) return "📅";
        if (value.contains("pending")) return "⏳";
        if (value.contains("approved")) return "✓";
        return "🅿";
    }

    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel header = new JPanel(); header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS)); header.setOpaque(false);
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 18)); t.setForeground(TEXT_DARK);
        JLabel s = new JLabel(subtitle); s.setFont(FONT_SUBTITLE); s.setForeground(TEXT_MUTED);
        header.add(t); header.add(javax.swing.Box.createVerticalStrut(4)); header.add(s);
        return header;
    }

    public static JPanel sidebarItem(String label, boolean active) {
        JPanel item = new JPanel(new BorderLayout()); item.setOpaque(true); item.setBackground(active ? PRIMARY_BLUE : PRIMARY_NAVY);
        item.setBorder(new EmptyBorder(12, 16, 12, 16)); item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel text = new JLabel(label); text.setFont(FONT_LABEL_BOLD); text.setForeground(TEXT_WHITE); item.add(text, BorderLayout.CENTER);
        if (!active) addHoverEffectToPanel(item);
        return item;
    }

    private static void addHoverEffectToPanel(JPanel panel) {
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { panel.setBackground(PRIMARY_NAVY_LIGHT); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { panel.setBackground(PRIMARY_NAVY); }
        });
    }

    public static void styleInput(JTextField field) { styleField(field); }
    public static void stylePassword(JPasswordField field) { styleField(field); }
    private static void styleField(javax.swing.text.JTextComponent field) {
        field.setFont(FONT_LABEL); field.setForeground(TEXT_DARK); field.setBackground(CARD_WHITE);
        field.setCaretColor(PRIMARY_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_LIGHT), new EmptyBorder(9, 11, 9, 11)));
    }

    public static void styleTable(javax.swing.JTable table) {
        table.setFont(FONT_TABLE_CELL); table.setForeground(TEXT_DARK); table.setBackground(CARD_WHITE);
        table.setRowHeight(38); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 234, 254)); table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true); table.setShowHorizontalLines(true); table.setGridColor(BACKGROUND);
        table.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(FONT_TABLE_HEADER); table.getTableHeader().setBackground(new Color(248, 250, 252));
            table.getTableHeader().setForeground(TEXT_MUTED); table.getTableHeader().setPreferredSize(new Dimension(0, 40));
            table.getTableHeader().setReorderingAllowed(false); table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));
        }
    }

    public static void styleScrollPane(javax.swing.JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scrollPane.getViewport().setBackground(CARD_WHITE);
        scrollPane.setBackground(CARD_WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    public static void styleDialog(Component parent) { installDefaults(); }

    public static void showToast(Component parent, String message) {
        JWindow toast = new JWindow(); toast.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(PRIMARY_NAVY_LIGHT);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(71, 85, 105)), new EmptyBorder(10, 16, 10, 16)));
        JLabel label = new JLabel(message); label.setFont(FONT_LABEL); label.setForeground(TEXT_WHITE); panel.add(label, BorderLayout.CENTER);
        toast.add(panel); toast.pack();
        if (parent != null && parent.isShowing()) {
            java.awt.Point point = parent.getLocationOnScreen();
            toast.setLocation(point.x + (parent.getWidth() - toast.getWidth()) / 2, point.y + parent.getHeight() - toast.getHeight() - 32);
        } else toast.setLocationRelativeTo(null);
        toast.setVisible(true); Timer timer = new Timer(3000, e -> toast.dispose()); timer.setRepeats(false); timer.start();
    }
}
