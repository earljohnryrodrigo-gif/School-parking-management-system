package ui;

import service.ParkingService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final ParkingService service;
    private final Runnable onAuthSuccess;

    private JTextField nameField;
    private JPasswordField passField;
    private JLabel inlineError;

    public LoginPanel(ParkingService service, Runnable onShowSignup, Runnable onAuthSuccess) {
        this(service, onShowSignup, onShowSignup, onAuthSuccess);
    }

    public LoginPanel(ParkingService service, Runnable onShowSignup, Runnable onShowVisitorSignup,
                      Runnable onAuthSuccess) {
        this.service = service;
        this.onAuthSuccess = onAuthSuccess;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(new BrandPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.CARD_WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        // Branding header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JLabel brandLabel = new JLabel("Parking-ka-BOSS");
        brandLabel.setFont(Theme.FONT_TITLE);
        brandLabel.setForeground(Theme.PRIMARY_NAVY);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("School Parking Management System");
        taglineLabel.setFont(Theme.FONT_SUBTITLE);
        taglineLabel.setForeground(Theme.TEXT_MUTED);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(Theme.TEXT_DARK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(brandLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(taglineLabel);
        header.add(Box.createVerticalStrut(20));
        header.add(heading);

        right.add(header, BorderLayout.NORTH);

        // Form card
        JPanel formCard = Theme.roundedPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JLabel nameLabel = new JLabel("Name / Username");
        nameLabel.setFont(Theme.FONT_LABEL);
        nameLabel.setForeground(Theme.TEXT_MUTED);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField = new JTextField();
        Theme.styleInput(nameField);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_LABEL);
        passLabel.setForeground(Theme.TEXT_MUTED);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel passContainer = new JPanel(new BorderLayout(0, 0));
        passContainer.setOpaque(false);
        passContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        passContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        passField = new JPasswordField();
        Theme.stylePassword(passField);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JToggleButton showPassBtn = new JToggleButton("\uD83D\uDC41");
        showPassBtn.setFont(new Font("segos UI Emoji", Font.PLAIN, 16));
        showPassBtn.setFocusPainted(false);
        showPassBtn.setBorderPainted(false);
        showPassBtn.setOpaque(false);
        showPassBtn.setForeground(Theme.TEXT_MUTED);
        showPassBtn.setMaximumSize(new Dimension(40, 44));
        showPassBtn.setPreferredSize(new Dimension(40, 44));
        showPassBtn.addActionListener(e -> {
            boolean show = showPassBtn.isSelected();
            passField.setEchoChar(show ? 0 : '\u25CF');
            showPassBtn.setText(show ? "\uD83D\uDC41" : "\uD83D\uDC42");
        });

        passContainer.add(passField, BorderLayout.CENTER);
        passContainer.add(showPassBtn, BorderLayout.EAST);

        // Inline error label
        inlineError = new JLabel(" ");
        inlineError.setFont(Theme.FONT_LABEL);
        inlineError.setForeground(Theme.STATUS_RED);
        inlineError.setAlignmentX(Component.LEFT_ALIGNMENT);
        inlineError.setVisible(false);

        formCard.add(nameLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(nameField);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passContainer);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(inlineError);
        formCard.add(Box.createVerticalStrut(20));

        right.add(formCard, BorderLayout.CENTER);

        // Action area
        JPanel actionArea = new JPanel();
        actionArea.setLayout(new BoxLayout(actionArea, BoxLayout.Y_AXIS));
        actionArea.setOpaque(false);
        actionArea.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        actionArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = Theme.primaryButton("Login");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton newAccountBtn = Theme.secondaryButton("Sign Up");
        newAccountBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newAccountBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        Theme.addHoverEffect(loginBtn);
        Theme.addHoverEffect(newAccountBtn);

        loginBtn.addActionListener(e -> attemptLogin());

        newAccountBtn.addActionListener(e -> onShowSignup.run());

        actionArea.add(loginBtn);
        actionArea.add(Box.createVerticalStrut(12));
        actionArea.add(newAccountBtn);

        right.add(actionArea, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);
    }

    private void attemptLogin() {
        inlineError.setVisible(false);
        String name = nameField.getText().trim();
        String pass = new String(passField.getPassword());

        if (name.isEmpty()) {
            showInlineError("Please enter your name / username.");
            nameField.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            showInlineError("Please enter your password.");
            passField.requestFocus();
            return;
        }

        // Auto-identify user role from the registered account -- no user type selection needed
        ParkingService.UserAccount account = service.login(name, pass);

        if (account != null) {
            nameField.setText("");
            passField.setText("");

            Theme.showToast(this, "Login successful! Welcome, " + account.getName() + ".");
            onAuthSuccess.run();

        } else {
            String registrationMessage = service.getLastLoginMessage();
            if (registrationMessage.isBlank()) {
                showInlineError("Invalid credentials.\n(Demo accounts: admin, student1, staff1, visitor1 | pass: 1234)");
            } else {
                JOptionPane.showMessageDialog(this, registrationMessage,
                        "Student Registration", JOptionPane.WARNING_MESSAGE);
                showInlineError(registrationMessage);
            }
        }
    }

    private void showInlineError(String message) {
        String safeMessage = message.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\n", "<br>");
        inlineError.setText("<html>" + safeMessage + "</html>");
        inlineError.setVisible(true);
    }
}
