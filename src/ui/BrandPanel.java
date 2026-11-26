package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BrandPanel extends JPanel {

    public BrandPanel() {
        setLayout(new GridBagLayout());
        setBackground(Theme.PRIMARY_NAVY);
        setPreferredSize(new Dimension(300, 0));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        URL logoURL = getClass().getResource("/images/parking-logo.jpg");

        if (logoURL != null) {
            ImageIcon icon = new ImageIcon(logoURL);

            Image img = icon.getImage().getScaledInstance(
                    200, 200, Image.SCALE_SMOOTH
            );

            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            stack.add(iconLabel);
            stack.add(Box.createVerticalStrut(20));
        }

        JLabel titleLabel = new JLabel("Parking-ka-BOSS", SwingConstants.CENTER);
        titleLabel.setForeground(Theme.TEXT_WHITE);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("School Parking Management", SwingConstants.CENTER);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        stack.add(titleLabel);
        stack.add(Box.createVerticalStrut(10));
        stack.add(subtitleLabel);

        add(stack);
    }
}