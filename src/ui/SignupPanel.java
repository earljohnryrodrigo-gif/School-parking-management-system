package ui;

import model.UserType;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;

public class SignupPanel extends JPanel {

    public SignupPanel(ParkingService service, Runnable onShowLogin, Runnable onAuthSuccess) {
        setLayout(new BorderLayout());
        add(new BrandPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(28, 45, 22, 45));

        JLabel heading = new JLabel("You Seem New Here!!", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.PLAIN, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel form = new JPanel(new GridLayout(8, 1, 4, 4));
        form.setOpaque(false);

        JComboBox<UserType> typeCombo = new JComboBox<>(UserType.values());
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        form.add(new JLabel("Choose User Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Password:"));
        form.add(passField);
        form.add(new JLabel("Confirm Password:"));
        form.add(confirmField);

        JPanel actionArea = new JPanel(new BorderLayout());
        actionArea.setOpaque(false);
        actionArea.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton signUpBtn = new JButton("SignUp");
        signUpBtn.setPreferredSize(new Dimension(140, 34));

        JPanel signUpCenter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        signUpCenter.setOpaque(false);
        signUpCenter.add(signUpBtn);

        JButton hasAccountBtn = new JButton("I have an account!");
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRight.setOpaque(false);
        bottomRight.add(hasAccountBtn);

        signUpBtn.addActionListener(e -> {
            UserType chosenType = (UserType) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both a name and password.", "Signup Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Signup Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean registered = service.registerAccount(name, pass, chosenType);
            if (!registered) {
                JOptionPane.showMessageDialog(this, "That account name already exists.", "Signup Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            service.login(name, pass, chosenType);
            nameField.setText("");
            passField.setText("");
            confirmField.setText("");

            JOptionPane.showMessageDialog(this, "Signup Successful", "Signup Successful", JOptionPane.INFORMATION_MESSAGE);
            onAuthSuccess.run();
        });

        hasAccountBtn.addActionListener(e -> onShowLogin.run());

        actionArea.add(signUpCenter, BorderLayout.CENTER);
        actionArea.add(bottomRight, BorderLayout.SOUTH);

        right.add(heading, BorderLayout.NORTH);
        right.add(form, BorderLayout.CENTER);
        right.add(actionArea, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);
    }
}