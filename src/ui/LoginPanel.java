package ui;

import model.UserType;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoginPanel extends JPanel {

    public LoginPanel(ParkingService service, Runnable onShowSignup, Runnable onAuthSuccess) {
        setLayout(new BorderLayout());
        add(new BrandPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(30, 45, 22, 45));

        JLabel heading = new JLabel("Welcome Back!!", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.PLAIN, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel form = new JPanel(new GridLayout(6, 1, 5, 5));
        form.setOpaque(false);

        JComboBox<UserType> typeCombo = new JComboBox<>(UserType.values());
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();

        form.add(new JLabel("Choose User Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Password:"));
        form.add(passField);

        JPanel actionArea = new JPanel(new BorderLayout());
        actionArea.setOpaque(false);
        actionArea.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(140, 34));

        JPanel loginCenter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginCenter.setOpaque(false);
        loginCenter.add(loginBtn);

        JButton newAccountBtn = new JButton("I am New!");
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRight.setOpaque(false);
        bottomRight.add(newAccountBtn);

        loginBtn.addActionListener(e -> {
            UserType chosenType = (UserType) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());

            ParkingService.UserAccount account = service.login(name, pass, chosenType);

            if (account != null) {
                nameField.setText("");
                passField.setText("");

                showLoginSuccess();

                onAuthSuccess.run();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid credentials or user type.\n(Demo accounts: admin, student1, staff1, visitor1 | pass: 1234)",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        newAccountBtn.addActionListener(e -> onShowSignup.run());

        actionArea.add(loginCenter, BorderLayout.CENTER);
        actionArea.add(bottomRight, BorderLayout.SOUTH);

        right.add(heading, BorderLayout.NORTH);
        right.add(form, BorderLayout.CENTER);
        right.add(actionArea, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);
    }

    private void showLoginSuccess() {

        URL logoURL = getClass().getResource("/images/approve.jpg");

        ImageIcon logoIcon = null;

        if (logoURL != null) {
            ImageIcon originalIcon = new ImageIcon(logoURL);

            Image image = originalIcon.getImage().getScaledInstance(
                    220, 200, Image.SCALE_SMOOTH
            );

            logoIcon = new ImageIcon(image);
        }

        JOptionPane.showMessageDialog(
                this,
                "Login Successful!",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE,
                logoIcon
        );
    }
}