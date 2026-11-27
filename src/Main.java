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
            frame.setUndecorated(false);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent event) {
                    for (Window window : Window.getWindows()) {
                        if (window != frame && window.isDisplayable()) window.dispose();
                    }
                    frame.dispose();
                    System.exit(0);
                }
            });
            frame.setSize(1120, 720);

            CardLayout cardLayout = new CardLayout();
            JPanel rootPanel = new JPanel(cardLayout);

            DashboardPanel[] dashboardRef = new DashboardPanel[1];

            SignupPanel studentSignupPanel = new SignupPanel(
                    service, model.UserType.STUDENT,
                    () -> cardLayout.show(rootPanel, "LOGIN"),
                    () -> cardLayout.show(rootPanel, "VISITOR_SIGNUP"),
                    () -> {
                        dashboardRef[0].refreshAll();
                        cardLayout.show(rootPanel, "DASHBOARD");
                    });
            SignupPanel visitorSignupPanel = new SignupPanel(
                    service, model.UserType.VISITOR,
                    () -> cardLayout.show(rootPanel, "LOGIN"),
                    () -> cardLayout.show(rootPanel, "SIGNUP"),
                    () -> {
                        dashboardRef[0].refreshAll();
                        cardLayout.show(rootPanel, "DASHBOARD");
                    });

            LoginPanel loginPanel = new LoginPanel(
                    service,
                    () -> cardLayout.show(rootPanel, "SIGNUP"),
                    () -> cardLayout.show(rootPanel, "VISITOR_SIGNUP"),
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

            rootPanel.add(studentSignupPanel, "SIGNUP");
            rootPanel.add(visitorSignupPanel, "VISITOR_SIGNUP");
            rootPanel.add(loginPanel, "LOGIN");
            rootPanel.add(dashboardPanel, "DASHBOARD");

            frame.add(rootPanel);
            cardLayout.show(rootPanel, "SIGNUP");
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
