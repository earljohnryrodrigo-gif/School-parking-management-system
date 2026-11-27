package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/** Collapsible dark navigation rail used by the dashboard. */
public final class NavigationSidebar extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int EXPANDED_WIDTH = 260;
    private static final int COLLAPSED_WIDTH = 68;
    private final JPanel itemsPanel = new JPanel();
    private final JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
    private final JLabel brandMark = new JLabel("P", JLabel.CENTER);
    private final JLabel brandName = new JLabel("Parking-ka-BOSS");
    private final JButton toggleButton = new JButton("<");
    private final JButton logoutButton;
    private final List<Item> items = new ArrayList<>();
    private boolean collapsed;
    private String selectedLabel;
    private Timer resizeTimer;

    public NavigationSidebar(Runnable onLogout) {
        super(new BorderLayout());
        setBackground(Theme.PRIMARY_NAVY);
        setBorder(new EmptyBorder(12, 8, 12, 8));
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        brandPanel.setOpaque(false);
        brandMark.setOpaque(true); brandMark.setBackground(Theme.PRIMARY_BLUE); brandMark.setForeground(Color.WHITE);
        brandMark.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandMark.setPreferredSize(new Dimension(38, 38));
        brandName.setForeground(Color.WHITE); brandName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandPanel.add(brandMark, BorderLayout.WEST); brandPanel.add(brandName, BorderLayout.CENTER);
        toggleButton.setText("<"); toggleButton.setToolTipText("Collapse sidebar");
        toggleButton.setForeground(new Color(203, 213, 225)); toggleButton.setBackground(Theme.PRIMARY_NAVY_LIGHT);
        toggleButton.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8)); toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> animateToggle());
        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); toggleRow.setOpaque(false); toggleRow.add(toggleButton);
        JPanel brandBlock = new JPanel(new BorderLayout()); brandBlock.setOpaque(false);
        brandBlock.add(brandPanel, BorderLayout.CENTER); brandBlock.add(toggleRow, BorderLayout.EAST);
        top.add(brandBlock, BorderLayout.NORTH);

        itemsPanel.setOpaque(false); itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        JScrollPane itemsScroll = new JScrollPane(itemsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        itemsScroll.setBorder(BorderFactory.createEmptyBorder());
        itemsScroll.setOpaque(false);
        itemsScroll.getViewport().setOpaque(false);
        itemsScroll.getVerticalScrollBar().setUnitIncrement(18);
        itemsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        itemsScroll.getVerticalScrollBar().setBackground(Theme.PRIMARY_NAVY);
        itemsScroll.getVerticalScrollBar().setForeground(new Color(71, 85, 105));
        top.add(itemsScroll, BorderLayout.CENTER);
        add(top, BorderLayout.CENTER);

        logoutButton = navButton("OUT", "Logout", onLogout);
        logoutButton.setForeground(new Color(254, 202, 202));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));
        bottom.add(logoutButton, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setItems(List<NavItem> newItems, String selected) {
        items.clear(); selectedLabel = selected; itemsPanel.removeAll();
        for (NavItem item : newItems) {
            JButton button = navButton(item.icon, item.label, item.action);
            items.add(new Item(item, button));
            itemsPanel.add(button); itemsPanel.add(Box.createVerticalStrut(5));
        }
        refreshAppearance(); revalidate(); repaint();
    }

    public void setSelected(String label) { selectedLabel = label; refreshAppearance(); }
    public boolean isCollapsed() { return collapsed; }

    public void setBadge(String label, int count) {
        for (Item item : items) {
            if (item.data.label.equals(label)) item.badgeCount = Math.max(0, count);
        }
        refreshAppearance();
    }

    private JButton navButton(String icon, String label, Runnable action) {
        JButton button = new JButton(); button.setHorizontalAlignment(JButton.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12)); button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 8, 9, 8)); button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); button.setAlignmentX(LEFT_ALIGNMENT);
        button.addActionListener(e -> action.run());
        button.putClientProperty("nav.icon", icon); button.putClientProperty("nav.label", label);
        return button;
    }

    private void refreshAppearance() {
        for (Item item : items) {
            boolean active = item.data.label.equals(selectedLabel);
            JButton button = item.button;
            if (collapsed) {
                button.setText(item.data.icon);
                button.setHorizontalAlignment(JButton.CENTER);
                button.setToolTipText(item.badgeCount > 0
                        ? item.data.label + " (" + item.badgeCount + " pending)" : item.data.label);
            } else {
                button.setHorizontalAlignment(JButton.LEFT);
                String badge = item.badgeCount > 0
                        ? " <span style='background-color:#DC2626;color:white'>&nbsp;" + item.badgeCount + "&nbsp;</span>" : "";
                button.setText("<html><nobr><table cellpadding='0' cellspacing='0'><tr><td width='26' align='center'>"
                        + item.data.icon + "</td><td>" + item.data.label + badge + "</td></tr></table></nobr></html>");
                button.setToolTipText(null);
            }
            button.setBackground(active ? Theme.PRIMARY_BLUE : Theme.PRIMARY_NAVY);
            button.setForeground(active ? Color.WHITE : new Color(203, 213, 225));
            button.setOpaque(true); button.setContentAreaFilled(true); button.setBorderPainted(false);
            button.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        }
        logoutButton.setText(collapsed ? "OUT" : "  OUT     Logout");
        logoutButton.setToolTipText(collapsed ? "Logout" : null);
        logoutButton.setBackground(Theme.PRIMARY_NAVY_LIGHT); logoutButton.setOpaque(true); logoutButton.setContentAreaFilled(true);
        brandName.setVisible(!collapsed);
        toggleButton.setText(collapsed ? ">" : "<"); toggleButton.setToolTipText(collapsed ? "Expand sidebar" : "Collapse sidebar");
    }

    private void animateToggle() {
        int start = getPreferredSize().width;
        int target = collapsed ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        collapsed = !collapsed; refreshAppearance();
        if (resizeTimer != null && resizeTimer.isRunning()) resizeTimer.stop();
        resizeTimer = new Timer(12, null);
        final int[] step = {0};
        resizeTimer.addActionListener(e -> {
            step[0]++;
            double fraction = Math.min(1.0, step[0] / 12.0);
            int width = (int) Math.round(start + (target - start) * fraction);
            setPreferredSize(new Dimension(width, 0)); revalidate();
            if (fraction >= 1.0) resizeTimer.stop();
        });
        resizeTimer.start();
    }

    public static final class NavItem {
        private final String icon, label;
        private final Runnable action;
        public NavItem(String icon, String label, Runnable action) { this.icon = icon; this.label = label; this.action = action; }
    }
    private static final class Item {
        final NavItem data; final JButton button;
        int badgeCount;
        Item(NavItem data, JButton button) { this.data = data; this.button = button; }
    }
}
