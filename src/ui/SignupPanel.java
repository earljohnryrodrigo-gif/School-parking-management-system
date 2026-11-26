package ui;

import model.UserType;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;

public class SignupPanel extends JPanel {

    private static final UserType[] PUBLIC_ROLES = {UserType.STUDENT, UserType.VISITOR, UserType.SCHOOL_PERSONNEL};

    private final ParkingService service;
    private final Runnable onShowLogin;
    private final Runnable onAuthSuccess;

    private JComboBox<UserType> typeCombo;
    private JTextField nameField;
    private JPasswordField passField;
    private JPasswordField confirmField;
    private JLabel inlineError;
    private JTextField profileField;
    private JLabel profileLabel;

    public SignupPanel(ParkingService service, Runnable onShowLogin, Runnable onAuthSuccess) {
        this.service = service;
        this.onShowLogin = onShowLogin;
        this.onAuthSuccess = onAuthSuccess;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(new BrandPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.CARD_WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(28, 45, 28, 45));

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

        JLabel heading = new JLabel("Create Account");
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

        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(Theme.FONT_LABEL);
        typeLabel.setForeground(Theme.TEXT_MUTED);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        typeCombo = new JComboBox<>(PUBLIC_ROLES);
        typeCombo.setFont(Theme.FONT_LABEL);
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCombo.addActionListener(e -> updateProfilePrompt());

        JLabel nameLabel = new JLabel("Name / Username");
        nameLabel.setFont(Theme.FONT_LABEL);
        nameLabel.setForeground(Theme.TEXT_MUTED);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField = new JTextField();
        Theme.styleInput(nameField);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileLabel = new JLabel("Student / visitor / personnel information");
        profileLabel.setFont(Theme.FONT_LABEL);
        profileLabel.setForeground(Theme.TEXT_MUTED);
        profileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileField = new JTextField();
        Theme.styleInput(profileField);
        profileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        profileField.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        showPassBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
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

        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(Theme.FONT_LABEL);
        confirmLabel.setForeground(Theme.TEXT_MUTED);
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel confirmContainer = new JPanel(new BorderLayout(0, 0));
        confirmContainer.setOpaque(false);
        confirmContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        confirmField = new JPasswordField();
        Theme.stylePassword(confirmField);
        confirmField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JToggleButton showConfirmBtn = new JToggleButton("\uD83D\uDC41");
        showConfirmBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        showConfirmBtn.setFocusPainted(false);
        showConfirmBtn.setBorderPainted(false);
        showConfirmBtn.setOpaque(false);
        showConfirmBtn.setForeground(Theme.TEXT_MUTED);
        showConfirmBtn.setMaximumSize(new Dimension(40, 44));
        showConfirmBtn.setPreferredSize(new Dimension(40, 44));
        showConfirmBtn.addActionListener(e -> {
            boolean show = showConfirmBtn.isSelected();
            confirmField.setEchoChar(show ? 0 : '\u25CF');
            showConfirmBtn.setText(show ? "\uD83D\uDC41" : "\uD83D\uDC42");
        });

        confirmContainer.add(confirmField, BorderLayout.CENTER);
        confirmContainer.add(showConfirmBtn, BorderLayout.EAST);

        // Inline error label
        inlineError = new JLabel(" ");
        inlineError.setFont(Theme.FONT_LABEL);
        inlineError.setForeground(Theme.STATUS_RED);
        inlineError.setAlignmentX(Component.LEFT_ALIGNMENT);
        inlineError.setVisible(false);

        formCard.add(typeLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(typeCombo);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(nameLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(nameField);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(profileLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(profileField);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passContainer);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(confirmLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(confirmContainer);
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

        JButton signUpBtn = Theme.primaryButton("Sign Up");
        signUpBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signUpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton hasAccountBtn = Theme.secondaryButton("I already have an account");
        hasAccountBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        hasAccountBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        Theme.addHoverEffect(signUpBtn);
        Theme.addHoverEffect(hasAccountBtn);

        signUpBtn.addActionListener(e -> attemptSignup());

        hasAccountBtn.addActionListener(e -> onShowLogin.run());

        actionArea.add(signUpBtn);
        actionArea.add(Box.createVerticalStrut(12));
        actionArea.add(hasAccountBtn);

        right.add(actionArea, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);
        updateProfilePrompt();
    }

    private void updateProfilePrompt() {
        UserType type = (UserType) typeCombo.getSelectedItem();
        if (profileLabel == null) return;
        if (type == UserType.STUDENT) profileLabel.setText("Student number / program");
        else if (type == UserType.SCHOOL_PERSONNEL) profileLabel.setText("Employee number / department");
        else profileLabel.setText("Visit purpose / contact information");
    }

    private void attemptSignup() {
        inlineError.setVisible(false);
        UserType chosenType = (UserType) typeCombo.getSelectedItem();
        String name = nameField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String profile = profileField.getText().trim();

        if (name.isEmpty() || pass.isEmpty() || profile.isEmpty()) {
            showInlineError("Please complete all required fields.");
            nameField.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            showInlineError("Passwords do not match.");
            passField.requestFocus();
            return;
        }
        boolean registered = service.registerAccount(name, pass, chosenType, profile);
        if (!registered) {
            showInlineError("That account name already exists.");
            return;
        }

        service.login(name, pass, chosenType);
        nameField.setText("");
        passField.setText("");
        confirmField.setText("");
        profileField.setText("");

        Theme.showToast(this, "Account created successfully! Welcome, " + name + ".");
        onAuthSuccess.run();
    }

    private void showInlineError(String message) {
        inlineError.setText("<html>" + message.replace("\n", "<br>") + "</html>");
        inlineError.setVisible(true);
    }
}
