package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BrandPanel extends JPanel {

    public BrandPanel() {
        setLayout(new GridBagLayout());
        setBackground(Theme.SIDEBAR);
        setPreferredSize(new Dimension(340, 0));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        URL logoURL = getClass().getResource("/images/parking-logo.jpg");

        if (logoURL != null) {
            ImageIcon icon = new ImageIcon(logoURL);

            Image img = icon.getImage().getScaledInstance(
                    300, 300, Image.SCALE_SMOOTH
            );

            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            stack.add(iconLabel);
            stack.add(Box.createVerticalStrut(12));
        }

        JLabel titleLabel = new JLabel("Parking-ka-BOSS", SwingConstants.CENTER);
        titleLabel.setForeground(Theme.ORANGE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("School Parking Management", SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(220, 220, 220));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        stack.add(titleLabel);
        stack.add(Box.createVerticalStrut(4));
        stack.add(subtitleLabel);

        add(stack);
    }
}