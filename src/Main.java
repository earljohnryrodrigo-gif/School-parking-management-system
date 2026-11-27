import service.ParkingService;
import ui.AccountTypeSelectionPanel;
import ui.DashboardPanel;
import ui.LoginPanel;
import ui.SignupPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ui.Theme.installDefaults();
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
            CardLayout cards = new CardLayout();
            JPanel root = new JPanel(cards);
            DashboardPanel[] dashboard = new DashboardPanel[1];
            Runnable showDashboard = () -> {
                dashboard[0].refreshAll();
                cards.show(root, "DASHBOARD");
            };
            AccountTypeSelectionPanel selection = new AccountTypeSelectionPanel(
                    () -> cards.show(root, "LOGIN"), type -> cards.show(root, "SIGNUP_" + type.name()));
            root.add(selection, "ACCOUNT_TYPES");
            root.add(new SignupPanel(service, model.UserType.STUDENT,
                    () -> cards.show(root, "ACCOUNT_TYPES"), null, showDashboard), "SIGNUP_STUDENT");
            root.add(new SignupPanel(service, model.UserType.SCHOOL_PERSONNEL,
                    () -> cards.show(root, "ACCOUNT_TYPES"), null, showDashboard), "SIGNUP_SCHOOL_PERSONNEL");
            root.add(new ui.VisitorSignupPanel(service,
                    () -> cards.show(root, "ACCOUNT_TYPES")), "SIGNUP_VISITOR");
            root.add(new LoginPanel(service, () -> cards.show(root, "ACCOUNT_TYPES"),
                    () -> cards.show(root, "ACCOUNT_TYPES"), showDashboard), "LOGIN");
            DashboardPanel mainDashboard = new DashboardPanel(service, () -> cards.show(root, "LOGIN"));
            dashboard[0] = mainDashboard;
            root.add(mainDashboard, "DASHBOARD");
            frame.add(root);
            cards.show(root, "LOGIN");
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
