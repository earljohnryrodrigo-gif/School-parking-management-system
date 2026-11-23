import service.ParkingService;
import ui.DashboardPanel;
import ui.LoginPanel;
import ui.SignupPanel;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParkingService service = new ParkingService();

            JFrame frame = new JFrame("Parking-ka-BOSS");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(920, 560);

            CardLayout cardLayout = new CardLayout();
            JPanel rootPanel = new JPanel(cardLayout);

            DashboardPanel[] dashboardRef = new DashboardPanel[1];

            SignupPanel signupPanel = new SignupPanel(
                    service,
                    () -> cardLayout.show(rootPanel, "LOGIN"),
                    () -> {
                        dashboardRef[0].refreshAll();
                        cardLayout.show(rootPanel, "DASHBOARD");
                    }
            );

            LoginPanel loginPanel = new LoginPanel(
                    service,
                    () -> cardLayout.show(rootPanel, "SIGNUP"),
                    () -> {
                        dashboardRef[0].refreshAll();
                        cardLayout.show(rootPanel, "DASHBOARD");
                    }
            );

            DashboardPanel dashboardPanel = new DashboardPanel(
                    service,
                    () -> cardLayout.show(rootPanel, "LOGIN")
            );
            dashboardRef[0] = dashboardPanel;

            rootPanel.add(signupPanel, "SIGNUP");
            rootPanel.add(loginPanel, "LOGIN");
            rootPanel.add(dashboardPanel, "DASHBOARD");

            frame.add(rootPanel);
            cardLayout.show(rootPanel, "SIGNUP");
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}