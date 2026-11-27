package ui;

import model.UserType;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;

/** Public Student registration and the existing Visitor signup flow. */
public class SignupPanel extends JPanel {
    private final ParkingService service;
    private final UserType signupType;
    private final Runnable onShowLogin;
    private final Runnable onShowOtherSignup;
    private final Runnable onAuthSuccess;

    private JTextField fullNameField;
    private JTextField studentIdField;
    private JTextField courseYearField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> vehicleTypeBox;
    private JTextField plateField;
    private JTextField visitorInfoField;
    private JLabel inlineError;

    /** Backward-compatible constructor; the public form defaults to Student registration. */
    public SignupPanel(ParkingService service, Runnable onShowLogin, Runnable onAuthSuccess) {
        this(service, UserType.STUDENT, onShowLogin, onShowLogin, onAuthSuccess);
    }

    public SignupPanel(ParkingService service, UserType signupType, Runnable onShowLogin,
                       Runnable onShowOtherSignup, Runnable onAuthSuccess) {
        if (signupType != UserType.STUDENT && signupType != UserType.VISITOR) {
            throw new IllegalArgumentException("Public signup supports Students and Visitors only.");
        }
        this.service = service;
        this.signupType = signupType;
        this.onShowLogin = onShowLogin;
        this.onShowOtherSignup = onShowOtherSignup;
        this.onAuthSuccess = onAuthSuccess;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(new BrandPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setBackground(Theme.CARD_WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        right.add(buildHeader(), BorderLayout.NORTH);

        JPanel formCard = Theme.roundedPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        if (signupType == UserType.STUDENT) buildStudentForm(formCard);
        else buildVisitorForm(formCard);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getViewport().setBackground(Theme.CARD_WHITE);
        formScroll.getVerticalScrollBar().setUnitIncrement(18);
        right.add(formScroll, BorderLayout.CENTER);
        right.add(buildActions(), BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel brand = new JLabel("Parking-ka-BOSS");
        brand.setFont(Theme.FONT_TITLE);
        brand.setForeground(Theme.PRIMARY_NAVY);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("School Parking Management System");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel heading = new JLabel(signupType == UserType.STUDENT ? "Student Registration" : "Visitor Sign Up");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(Theme.TEXT_DARK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(brand);
        header.add(Box.createVerticalStrut(3));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(8));
        header.add(heading);
        return header;
    }

    private void buildStudentForm(JPanel card) {
        JPanel studentSection = section("Student Information");
        fullNameField = input();
        studentIdField = input();
        courseYearField = input();
        addField(studentSection, "Full Name", fullNameField);
        addField(studentSection, "Student ID", studentIdField);
        addField(studentSection, "Course & Year", courseYearField);
        card.add(studentSection);
        card.add(Box.createVerticalStrut(12));

        JPanel accountSection = section("Account Information");
        usernameField = input();
        passwordField = passwordInput();
        confirmPasswordField = passwordInput();
        addField(accountSection, "Username", usernameField);
        addField(accountSection, "Password", passwordContainer(passwordField));
        addField(accountSection, "Confirm Password", passwordContainer(confirmPasswordField));
        card.add(accountSection);
        card.add(Box.createVerticalStrut(12));

        JPanel vehicleSection = section("Vehicle Information");
        vehicleTypeBox = new JComboBox<>(new String[]{"Motorcycle", "Car"});
        vehicleTypeBox.setFont(Theme.FONT_LABEL);
        plateField = input();
        addField(vehicleSection, "Vehicle Type", vehicleTypeBox);
        addField(vehicleSection, "Plate Number", plateField);
        card.add(vehicleSection);
        addErrorLabel(card);
    }

    private void buildVisitorForm(JPanel card) {
        JPanel visitorSection = section("Visitor Information");
        usernameField = input();
        visitorInfoField = input();
        passwordField = passwordInput();
        confirmPasswordField = passwordInput();
        addField(visitorSection, "Name / Username", usernameField);
        addField(visitorSection, "Visit purpose / contact information", visitorInfoField);
        addField(visitorSection, "Password", passwordContainer(passwordField));
        addField(visitorSection, "Confirm Password", passwordContainer(confirmPasswordField));
        card.add(visitorSection);
        addErrorLabel(card);
    }

    private JPanel section(String title) {
        JPanel section = Theme.roundedPanel();
        section.setLayout(new GridLayout(0, 2, 12, 9));
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT), title));
        return section;
    }

    private void addField(JPanel section, String label, JComponent input) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(Theme.FONT_LABEL);
        fieldLabel.setForeground(Theme.TEXT_MUTED);
        section.add(fieldLabel);
        section.add(input);
    }

    private JTextField input() {
        JTextField field = new JTextField();
        Theme.styleInput(field);
        field.setPreferredSize(new Dimension(240, 38));
        return field;
    }

    private JPasswordField passwordInput() {
        JPasswordField field = new JPasswordField();
        Theme.stylePassword(field);
        field.setPreferredSize(new Dimension(240, 38));
        return field;
    }

    private JPanel passwordContainer(JPasswordField field) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setPreferredSize(new Dimension(240, 38));
        JToggleButton visibility = new JToggleButton("\uD83D\uDC41");
        visibility.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
        visibility.setFocusPainted(false);
        visibility.setBorderPainted(false);
        visibility.setOpaque(false);
        visibility.setForeground(Theme.TEXT_MUTED);
        visibility.setPreferredSize(new Dimension(36, 38));
        visibility.addActionListener(e -> {
            boolean show = visibility.isSelected();
            field.setEchoChar(show ? 0 : '\u25CF');
            visibility.setText(show ? "\uD83D\uDC41" : "\uD83D\uDC42");
        });
        container.add(field, BorderLayout.CENTER);
        container.add(visibility, BorderLayout.EAST);
        return container;
    }

    private void addErrorLabel(JPanel card) {
        inlineError = new JLabel(" ");
        inlineError.setFont(Theme.FONT_LABEL);
        inlineError.setForeground(Theme.STATUS_RED);
        inlineError.setBorder(BorderFactory.createEmptyBorder(8, 2, 2, 2));
        inlineError.setVisible(false);
        card.add(Box.createVerticalStrut(6));
        card.add(inlineError);
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 7));
        actions.setOpaque(false);
        JButton submit = Theme.primaryButton(signupType == UserType.STUDENT
                ? "Submit Student Registration" : "Create Visitor Account");
        JButton login = Theme.secondaryButton("I already have an account");
        JButton otherSignup = Theme.secondaryButton(signupType == UserType.STUDENT
                ? "Visitor Sign Up" : "Student Sign Up");
        Theme.addHoverEffect(submit);
        Theme.addHoverEffect(login);
        Theme.addHoverEffect(otherSignup);
        submit.addActionListener(e -> submitSignup());
        login.addActionListener(e -> onShowLogin.run());
        otherSignup.addActionListener(e -> onShowOtherSignup.run());
        actions.add(submit);
        actions.add(login);
        actions.add(otherSignup);
        return actions;
    }

    private void submitSignup() {
        inlineError.setVisible(false);
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmation = new String(confirmPasswordField.getPassword());
        if (username.isBlank() || password.isBlank() || confirmation.isBlank()) {
            showInlineError("Please complete all required fields.");
            return;
        }
        if (!password.equals(confirmation)) {
            showInlineError("Passwords do not match.");
            confirmPasswordField.requestFocus();
            return;
        }

        if (signupType == UserType.STUDENT) {
            if (fullNameField.getText().isBlank() || studentIdField.getText().isBlank()
                    || courseYearField.getText().isBlank() || plateField.getText().isBlank()) {
                showInlineError("Please complete all required Student and vehicle fields.");
                return;
            }
            String result = service.submitStudentRegistration(fullNameField.getText(), studentIdField.getText(),
                    courseYearField.getText(), username, password,
                    String.valueOf(vehicleTypeBox.getSelectedItem()), plateField.getText());
            if (!"OK".equals(result)) {
                showInlineError(result);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Registration submitted! Please wait for Parking Administrator approval before logging in.",
                    "Registration Submitted", JOptionPane.INFORMATION_MESSAGE);
            onShowLogin.run();
            return;
        }

        if (visitorInfoField.getText().isBlank()) {
            showInlineError("Please complete all required fields.");
            return;
        }
        boolean created = service.registerAccount(username, password, UserType.VISITOR,
                visitorInfoField.getText().trim());
        if (!created) {
            showInlineError("That username already exists.");
            return;
        }
        service.login(username, password, UserType.VISITOR);
        Theme.showToast(this, "Account created successfully! Welcome, " + username + ".");
        onAuthSuccess.run();
    }

    private void showInlineError(String message) {
        inlineError.setText("<html>" + message.replace("\n", "<br>") + "</html>");
        inlineError.setVisible(true);
    }
}
