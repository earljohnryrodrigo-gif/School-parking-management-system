package ui;

import service.ParkingService;

import javax.swing.*;
import java.awt.*;

/** Public Visitor registration, which also submits the Visitor's first reservation. */
public final class VisitorSignupPanel extends JPanel {
    private final ParkingService service;
    private final Runnable onShowLogin;
    private final JTextField fullName = new JTextField();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();
    private final JComboBox<String> vehicleType = new JComboBox<>(
            new String[]{"Car", "Sedan", "SUV", "Van", "Motorcycle"});
    private final JTextField plateNumber = new JTextField();
    private final JTextField purpose = new JTextField();
    private final JTextField reservationDate = new JTextField();
    private final JLabel error = new JLabel(" ");

    public VisitorSignupPanel(ParkingService service, Runnable onShowLogin) {
        this.service = service;
        this.onShowLogin = onShowLogin;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(new BrandPanel(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Theme.CARD_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 36, 20, 36));
        JLabel heading = new JLabel("Visitor Registration", SwingConstants.CENTER);
        heading.setFont(Theme.FONT_TITLE);
        heading.setForeground(Theme.PRIMARY_NAVY);
        content.add(heading, BorderLayout.NORTH);

        JPanel fields = Theme.roundedPanel();
        fields.setLayout(new GridLayout(0, 2, 12, 8));
        fields.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        addField(fields, "Full Name", fullName);
        addField(fields, "Username", username);
        addField(fields, "Password", password);
        addField(fields, "Confirm Password", confirmPassword);
        addField(fields, "Vehicle Type", vehicleType);
        addField(fields, "Plate Number", plateNumber);
        addField(fields, "Purpose of Visit", purpose);
        addField(fields, "Reservation Date (yyyy-MM-dd)", reservationDate);
        error.setForeground(Theme.STATUS_RED);
        addField(fields, "", error);
        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getViewport().setBackground(Theme.CARD_WHITE);
        content.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton back = Theme.secondaryButton("Back to Account Types");
        JButton submit = Theme.primaryButton("Create Visitor Account");
        back.addActionListener(e -> onShowLogin.run());
        submit.addActionListener(e -> submitRegistration());
        actions.add(back);
        actions.add(submit);
        content.add(actions, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, String label, JComponent component) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(Theme.FONT_LABEL);
        fieldLabel.setForeground(Theme.TEXT_MUTED);
        panel.add(fieldLabel);
        if (component instanceof JTextField textField) Theme.styleInput(textField);
        if (component instanceof JPasswordField passwordField) Theme.stylePassword(passwordField);
        if (component instanceof JComboBox<?>) component.setFont(Theme.FONT_LABEL);
        component.setPreferredSize(new Dimension(220, 36));
        panel.add(component);
    }

    private void submitRegistration() {
        String passwordText = new String(password.getPassword());
        String confirmation = new String(confirmPassword.getPassword());
        if (fullName.getText().isBlank() || username.getText().isBlank() || passwordText.isBlank()
                || confirmation.isBlank() || plateNumber.getText().isBlank() || purpose.getText().isBlank()
                || reservationDate.getText().isBlank()) {
            showError("Complete all required fields.");
            return;
        }
        if (!passwordText.equals(confirmation)) {
            showError("Passwords do not match.");
            return;
        }

        String result = service.submitVisitorRegistration(fullName.getText(), username.getText(), passwordText,
                String.valueOf(vehicleType.getSelectedItem()), plateNumber.getText(), purpose.getText(),
                reservationDate.getText());
        if (!"OK".equals(result)) {
            showError(result);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Visitor account created. You can log in now. Your parking reservation is pending Administrator approval.",
                "Visitor Registration Submitted", JOptionPane.INFORMATION_MESSAGE);
        onShowLogin.run();
    }

    private void showError(String message) {
        error.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;")
                .replace("\n", "<br>") + "</html>");
    }
}
