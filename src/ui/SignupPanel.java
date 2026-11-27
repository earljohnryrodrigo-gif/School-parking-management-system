package ui;

import model.UserType;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;

/** Role-specific public registration forms. */
public class SignupPanel extends JPanel {
    private final ParkingService service;
    private final UserType signupType;
    private final Runnable onShowLogin;
    private JTextField fullName, idField, detailField, username, plate;
    private JPasswordField password, confirm;
    private JComboBox<String> vehicleType;
    private JLabel error;

    /** Backward-compatible constructor for existing callers. */
    public SignupPanel(ParkingService service, Runnable onShowLogin, Runnable onAuthSuccess) {
        this(service, UserType.STUDENT, onShowLogin, onShowLogin, onAuthSuccess);
    }

    public SignupPanel(ParkingService service, UserType type, Runnable onShowLogin,
                       Runnable ignoredOtherSignup, Runnable onAuthSuccess) {
        if (type != UserType.STUDENT && type != UserType.SCHOOL_PERSONNEL)
            throw new IllegalArgumentException("Public signup supports Student and School Personnel.");
        this.service=service; this.signupType=type; this.onShowLogin=onShowLogin;
        setLayout(new BorderLayout()); setBackground(Theme.BACKGROUND); add(new BrandPanel(), BorderLayout.WEST);
        JPanel right=new JPanel(new BorderLayout(0,10)); right.setBackground(Theme.CARD_WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(20,36,20,36));
        String icon = type == UserType.STUDENT ? "🎓  " : "🧑‍💼  ";
        JLabel heading=new JLabel(icon + type.getLabel()+" Registration",SwingConstants.CENTER);
        heading.setFont(Theme.FONT_TITLE); heading.setForeground(Theme.PRIMARY_NAVY); right.add(heading,BorderLayout.NORTH);
        JPanel form=Theme.roundedPanel(); form.setLayout(new GridLayout(0,2,12,8));
        form.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
        fullName=field(form,"Full Name");
        if(type==UserType.STUDENT) { idField=field(form,"Student ID"); detailField=field(form,"Course & Year"); }
        if(type==UserType.SCHOOL_PERSONNEL) { idField=field(form,"Personnel/Employee ID"); detailField=field(form,"Department"); }
        username=field(form,"Username"); password=pwd(form,"Password"); confirm=pwd(form,"Confirm Password");
        vehicleType=new JComboBox<>(new String[]{"Motorcycle","Car"});
        addField(form,"Initial Vehicle Type",vehicleType);
        plate=field(form,"Plate Number");
        error=new JLabel(" "); error.setForeground(Theme.STATUS_RED); addField(form,"",error);
        JScrollPane scroller=new JScrollPane(form); scroller.setBorder(BorderFactory.createEmptyBorder()); scroller.getViewport().setBackground(Theme.CARD_WHITE);
        right.add(scroller,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton back=Theme.secondaryButton("Back to Account Types"), submit=Theme.primaryButton("Submit Registration");
        back.addActionListener(e->onShowLogin.run()); submit.addActionListener(e->submit()); actions.add(back); actions.add(submit);
        right.add(actions,BorderLayout.SOUTH); add(right,BorderLayout.CENTER);
    }

    private JTextField field(JPanel panel,String label){ JTextField f=new JTextField(); Theme.styleInput(f); f.setPreferredSize(new Dimension(220,36)); addField(panel,label,f); return f; }
    private JPasswordField pwd(JPanel panel,String label){ JPasswordField f=new JPasswordField(); Theme.stylePassword(f); f.setPreferredSize(new Dimension(220,36)); addField(panel,label,f); return f; }
    private void addField(JPanel p,String label,JComponent component){ JLabel l=new JLabel(label); l.setFont(Theme.FONT_LABEL); l.setForeground(Theme.TEXT_MUTED); p.add(l); p.add(component); }

    private void submit(){
        String pass=new String(password.getPassword()), confirmPass=new String(confirm.getPassword());
        if(pass.isBlank()||confirmPass.isBlank()||fullName.getText().isBlank()||username.getText().isBlank()||plate.getText().isBlank()
                || idField.getText().isBlank()||detailField.getText().isBlank()){ showError("Complete all required fields."); return; }
        if(!pass.equals(confirmPass)){showError("Passwords do not match.");return;}
        String result;
        if(signupType==UserType.STUDENT) result=service.submitStudentRegistration(fullName.getText(),idField.getText(),detailField.getText(),username.getText(),pass,String.valueOf(vehicleType.getSelectedItem()),plate.getText());
        else result=service.submitPersonnelRegistration(fullName.getText(),idField.getText(),detailField.getText(),username.getText(),pass,String.valueOf(vehicleType.getSelectedItem()),plate.getText());
        if(!"OK".equals(result)){showError(result);return;}
        JOptionPane.showMessageDialog(this,"Registration submitted! Please wait for Parking Administrator approval before logging in.","Registration Submitted",JOptionPane.INFORMATION_MESSAGE);
        onShowLogin.run();
    }
    private void showError(String text){error.setText("<html>"+text.replace("&","&amp;").replace("<","&lt;").replace("\n","<br>")+"</html>");}
}
