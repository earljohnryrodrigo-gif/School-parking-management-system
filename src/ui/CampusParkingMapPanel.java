package ui;

import model.CampusMapLayout;
import model.UserType;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/** Static campus location guide. It never reads or displays live parking availability. */
public final class CampusParkingMapPanel extends JPanel {
    private final ParkingService service;
    private final MapCanvas mapCanvas;
    private final JCheckBox editToggle = new JCheckBox("Edit map layout");
    private final JLabel editHint = new JLabel("View-only campus guide");

    public CampusParkingMapPanel(ParkingService service) {
        this.service = service;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);
        JLabel mapIcon = new JLabel("🗺");
        mapIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel title = new JLabel("Campus Parking Map");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.PRIMARY_NAVY);
        JLabel subtitle = new JLabel("Campus locations guide · not a live availability map");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(3));
        titleStack.add(subtitle);
        heading.add(mapIcon, BorderLayout.WEST);
        heading.add(titleStack, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        controls.setOpaque(false);
        JButton zoomOut = Theme.secondaryButton("−");
        JButton zoomIn = Theme.secondaryButton("+");
        JButton guide = Theme.secondaryButton("Map Guide");
        editToggle.setOpaque(false);
        editToggle.setFont(Theme.FONT_LABEL);
        editToggle.setForeground(Theme.TEXT_DARK);
        controls.add(editToggle);
        controls.add(zoomOut);
        controls.add(zoomIn);
        controls.add(guide);
        heading.add(controls, BorderLayout.EAST);

        mapCanvas = new MapCanvas();
        JScrollPane scroll = new JScrollPane(mapCanvas);
        Theme.styleScrollPane(scroll);
        scroll.getViewport().setBackground(new Color(226, 237, 230));
        JPanel canvasCard = Theme.roundedPanel();
        canvasCard.setLayout(new BorderLayout());
        canvasCard.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        canvasCard.add(scroll, BorderLayout.CENTER);

        JPanel legend = buildLegend();
        add(heading, BorderLayout.NORTH);
        add(canvasCard, BorderLayout.CENTER);
        add(legend, BorderLayout.SOUTH);

        zoomIn.addActionListener(e -> mapCanvas.changeZoom(0.12));
        zoomOut.addActionListener(e -> mapCanvas.changeZoom(-0.12));
        guide.addActionListener(e -> showGuide());
        editToggle.addActionListener(e -> {
            boolean allowed = isAdministrator() && editToggle.isSelected();
            mapCanvas.setEditing(allowed);
            editHint.setText(allowed ? "Drag map markers to adjust locations" : "View-only campus guide");
        });
        setRole(service.getCurrentUser() == null ? null : service.getCurrentUser().getUserType());
    }

    public void setRole(UserType role) {
        boolean admin = role == UserType.PARKING_ADMINISTRATOR && isAdministrator();
        editToggle.setVisible(admin);
        if (!admin) editToggle.setSelected(false);
        mapCanvas.setEditing(admin && editToggle.isSelected());
        editHint.setText(mapCanvas.isEditing() ? "Drag map markers to adjust locations" : "View-only campus guide");
        mapCanvas.refreshLayout();
    }

    private boolean isAdministrator() {
        return service.getCurrentUser() != null
                && service.getCurrentUser().getUserType() == UserType.PARKING_ADMINISTRATOR;
    }

    private JPanel buildLegend() {
        JPanel legend = Theme.roundedPanel();
        legend.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 4));
        legend.add(legendItem(new Color(219, 234, 254), "Parking Zones"));
        legend.add(legendItem(new Color(255, 237, 213), "Two-Wheeler Zone"));
        legend.add(legendItem(new Color(255, 255, 255), "Campus Buildings"));
        legend.add(legendItem(new Color(220, 252, 231), "Walkways & Green Areas"));
        legend.add(editHint);
        return legend;
    }

    private JPanel legendItem(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
        JLabel swatch = new JLabel("  ");
        swatch.setOpaque(true);
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(Theme.BORDER_MEDIUM));
        JLabel text = new JLabel(label);
        text.setFont(Theme.FONT_LABEL);
        text.setForeground(Theme.TEXT_MUTED);
        item.add(swatch);
        item.add(text);
        return item;
    }

    private void showGuide() {
        String message = "Use this map to find campus buildings and parking zones.\n"
                + "It is a location guide only and does not show live slot availability.\n"
                + (isAdministrator() ? "Turn on Edit map layout, then drag a marker to reposition it." : "The map is view-only for your role.");
        JOptionPane.showMessageDialog(this, message, "Campus Map Guide", JOptionPane.INFORMATION_MESSAGE);
    }

    private final class MapCanvas extends JPanel {
        private static final int BASE_WIDTH = 1050;
        private static final int BASE_HEIGHT = 650;
        private final List<Landmark> landmarks = createLandmarks();
        private double zoom = 1.0;
        private boolean editing;
        private String draggingId;

        MapCanvas() {
            setBackground(new Color(226, 237, 230));
            setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (!canEdit()) return;
                    Landmark landmark = hitTest(e.getPoint());
                    draggingId = landmark == null ? null : landmark.id;
                }
                @Override public void mouseDragged(MouseEvent e) {
                    if (draggingId == null || !canEdit()) return;
                    double x = Math.max(0.12, Math.min(0.88, e.getX() / (double) getWidth()));
                    double y = Math.max(0.12, Math.min(0.88, e.getY() / (double) getHeight()));
                    if (service.moveCampusMapLandmark(draggingId, x, y)) refreshLayout();
                }
                @Override public void mouseReleased(MouseEvent e) { draggingId = null; }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        boolean isEditing() { return editing; }

        void setEditing(boolean enabled) {
            editing = enabled && isAdministrator();
            setCursor(editing ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        private boolean canEdit() { return editing && isAdministrator(); }

        void refreshLayout() { repaint(); }

        void changeZoom(double amount) {
            zoom = Math.max(0.82, Math.min(1.55, zoom + amount));
            setPreferredSize(new Dimension((int) (BASE_WIDTH * zoom), (int) (BASE_HEIGHT * zoom)));
            revalidate();
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawGround(g);
            drawRoads(g);
            drawWalkways(g);
            drawTrees(g);
            for (Landmark landmark : landmarks) drawLandmark(g, landmark);
            g.dispose();
        }

        private void drawGround(Graphics2D g) {
            g.setColor(new Color(226, 237, 230));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(220, 252, 231, 125));
            g.fillRoundRect(22, 22, getWidth() - 44, getHeight() - 44, 30, 30);
            g.setColor(new Color(187, 213, 195));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(22, 22, getWidth() - 44, getHeight() - 44, 30, 30);
        }

        private void drawRoads(Graphics2D g) {
            g.setStroke(new BasicStroke(34, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(203, 213, 225));
            Path2D road = new Path2D.Double();
            road.moveTo(55, getHeight() * 0.50); road.lineTo(getWidth() - 55, getHeight() * 0.50);
            road.moveTo(getWidth() * 0.50, 45); road.lineTo(getWidth() * 0.50, getHeight() - 45);
            g.draw(road);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 12f}, 0f));
            g.setColor(Color.WHITE);
            g.draw(road);
            g.setStroke(new BasicStroke(2f));
        }

        private void drawWalkways(Graphics2D g) {
            g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(148, 163, 184));
            Path2D path = new Path2D.Double();
            path.moveTo(getWidth() * 0.20, getHeight() * 0.24);
            path.lineTo(getWidth() * 0.38, getHeight() * 0.36);
            path.lineTo(getWidth() * 0.48, getHeight() * 0.40);
            path.lineTo(getWidth() * 0.62, getHeight() * 0.60);
            path.lineTo(getWidth() * 0.79, getHeight() * 0.78);
            g.draw(path);
            g.setColor(new Color(248, 250, 252));
            g.setStroke(new BasicStroke(3));
            g.draw(path);
        }

        private void drawTrees(Graphics2D g) {
            int[][] positions = {{80,90},{165,500},{260,130},{315,530},{430,105},{580,115},{740,110},
                    {925,120},{925,340},{915,535},{740,535},{610,520},{480,550},{250,335},{100,360}};
            for (int[] p : positions) {
                int x = Math.min(getWidth() - 24, p[0] * getWidth() / BASE_WIDTH);
                int y = Math.min(getHeight() - 24, p[1] * getHeight() / BASE_HEIGHT);
                g.setColor(new Color(22, 101, 52)); g.fillOval(x - 9, y - 9, 18, 18);
                g.setColor(new Color(134, 239, 172)); g.fillOval(x - 6, y - 8, 10, 10);
            }
        }

        private void drawLandmark(Graphics2D g, Landmark landmark) {
            CampusMapLayout layout = service.getCampusMapLayout();
            if (layout == null) return;
            CampusMapLayout.Point position = layout.getLocation(landmark.id);
            if (position == null) return;
            int centerX = (int) (position.getX() * getWidth());
            int centerY = (int) (position.getY() * getHeight());
            int x = centerX - landmark.width / 2;
            int y = centerY - landmark.height / 2;
            g.setColor(new Color(15, 23, 42, 32));
            g.fillRoundRect(x + 3, y + 4, landmark.width, landmark.height, 18, 18);
            g.setColor(landmark.fill);
            g.fillRoundRect(x, y, landmark.width, landmark.height, 18, 18);
            g.setColor(landmark.border);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(x, y, landmark.width, landmark.height, 18, 18);
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 19));
            drawCentered(g, landmark.icon, centerX, y + 26);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            drawCentered(g, landmark.label, centerX, y + 47);
            if (canEdit()) {
                g.setColor(new Color(37, 99, 235));
                g.fillOval(x + landmark.width - 17, y + 6, 9, 9);
            }
        }

        private void drawCentered(Graphics2D g, String text, int x, int y) {
            FontMetrics metrics = g.getFontMetrics();
            g.setColor(Theme.TEXT_DARK);
            g.drawString(text, x - metrics.stringWidth(text) / 2, y);
        }

        private Landmark hitTest(Point point) {
            CampusMapLayout layout = service.getCampusMapLayout();
            if (layout == null) return null;
            for (int i = landmarks.size() - 1; i >= 0; i--) {
                Landmark landmark = landmarks.get(i);
                CampusMapLayout.Point location = layout.getLocation(landmark.id);
                int x = (int) (location.getX() * getWidth());
                int y = (int) (location.getY() * getHeight());
                Rectangle bounds = new Rectangle(x - landmark.width / 2, y - landmark.height / 2,
                        landmark.width, landmark.height);
                if (bounds.contains(point)) return landmark;
            }
            return null;
        }

        private List<Landmark> createLandmarks() {
            List<Landmark> list = new ArrayList<>();
            list.add(new Landmark("STUDENT_ZONE", "Student Parking Zone", "🅿", new Color(219,234,254), new Color(96,165,250), 188, 76));
            list.add(new Landmark("PERSONNEL_ZONE", "School Personnel Parking Zone", "🅿", new Color(224,231,255), new Color(129,140,248), 198, 76));
            list.add(new Landmark("VISITOR_ZONE", "Visitor Parking Zone", "🅿", new Color(254,243,199), new Color(251,191,36), 176, 76));
            list.add(new Landmark("TWO_WHEELER_ZONE", "Two-Wheeler Parking Zone", "🛵", new Color(255,237,213), new Color(251,146,60), 180, 76));
            list.add(new Landmark("MAIN_BUILDING", "Main Building", "🏫", Color.WHITE, new Color(148,163,184), 160, 76));
            list.add(new Landmark("LIBRARY", "Library", "📚", Color.WHITE, new Color(148,163,184), 145, 76));
            list.add(new Landmark("GYMNASIUM", "Gymnasium", "🏀", Color.WHITE, new Color(148,163,184), 150, 76));
            list.add(new Landmark("ADMIN_BUILDING", "Administration Building", "🏛", Color.WHITE, new Color(148,163,184), 168, 76));
            return list;
        }
    }

    private static final class Landmark {
        final String id, label, icon;
        final Color fill, border;
        final int width, height;
        Landmark(String id, String label, String icon, Color fill, Color border, int width, int height) {
            this.id = id; this.label = label; this.icon = icon; this.fill = fill; this.border = border;
            this.width = width; this.height = height;
        }
    }
}
